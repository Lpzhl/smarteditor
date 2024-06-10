package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.constant.AuthorityConstant;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.exception.GlobalExceptionHandler;
import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.DocumentFolder;
import hope.smarteditor.common.model.entity.Folder;
import hope.smarteditor.common.model.entity.FolderOperationLog;
import hope.smarteditor.common.model.vo.UserFolderInfoVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.HandleException;
import hope.smarteditor.document.mapper.DocumentFolderMapper;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.mapper.mapper.FolderOperationLogMapper;
import hope.smarteditor.document.service.FolderService;
import hope.smarteditor.document.mapper.FolderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author LoveF
* @description 针对表【folder】的数据库操作Service实现
* @createDate 2024-06-08 14:02:31
*/
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
    implements FolderService{

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private DocumentFolderMapper documentFolderMapper;

    @Autowired
    private FolderOperationLogMapper folderOperationLogMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Override
    public boolean createFolder(FolderDTO folderDTO) {
        try {

            // 1.创建文件夹
            Folder folder = new Folder();
            BeanUtils.copyProperties(folderDTO,folder);
            folder.setPermissions(AuthorityConstant.VIEW);
            int insert = folderMapper.insert(folder);

            // 2.创建操作日志
            FolderOperationLog folderOperationLog = new FolderOperationLog();
            folderOperationLog.setFolderId(folder.getId());
            folderOperationLog.setOperation(MessageConstant.CREATE_FOLDER);
            folderOperationLog.setUserId(folderDTO.getUserId());
            folderOperationLogMapper.insert(folderOperationLog);

            return insert>0;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
    }

    @Override
    @HandleException
    public boolean deleteFolder(Long folderId) {
        int i = folderMapper.deleteById(folderId);
        return i > 0;
    }

    @Override
    @HandleException
    public boolean updateFolder(FolderUpdateDTO folderDTO,Long userId) {
        // 1.更新文件夹名字
        Folder folder = new Folder();
        BeanUtils.copyProperties(folderDTO, folder);

        // 2.创建操作日志
        FolderOperationLog folderOperationLog = new FolderOperationLog();
        folderOperationLog.setFolderId(folderDTO.getId());
        folderOperationLog.setOperation(MessageConstant.UPDATE_FOLDER);
        folderOperationLog.setUserId(userId);
        folderOperationLogMapper.insert(folderOperationLog);
        return folderMapper.updateById(folder) > 0;
    }

    @Override
    @HandleException
    public boolean setFolderPermission(FolderPermissionUpdateDTO folderDTO) {
        Folder folder = new Folder();
        BeanUtils.copyProperties(folderDTO, folder);
        folder.setPermissions(folderDTO.getPermission() == 1 ? AuthorityConstant.VIEW : AuthorityConstant.EDIT);
        int i = folderMapper.updateById(folder);
        return i > 0;
    }

    @Override
    @HandleException
    public boolean createDocument(Long folderId, Document document, Long userId) {
        // 1.创建文件夹与文档关联
        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setFolderId(folderId);
        documentFolder.setDocumentId(document.getId());
        int insert = documentFolderMapper.insert(documentFolder);

        // 2.创建操作日志
        FolderOperationLog operationLog = new FolderOperationLog();
        operationLog.setFolderId(folderId);
        operationLog.setOperation(MessageConstant.CREATE_DOCUMENT);
        operationLog.setUserId(userId);
        operationLog.setDocumentName(document.getName());
        folderOperationLogMapper.insert(operationLog);
        return insert > 0;
    }

    @Override
    @HandleException
    public boolean deleteDocument(Long documentId) {
        int i = documentFolderMapper.delete(new QueryWrapper<DocumentFolder>().eq("document_id", documentId));
        return i > 0;
    }

    @Override
    public boolean moveDocument(MoveDocumentDTO moveDocumentDTO,Long userId) {
        Long documentId = moveDocumentDTO.getDocumentId();
        Long sourceFolderId = moveDocumentDTO.getSourceFolderId();
        Long targetFolderId = moveDocumentDTO.getTargetFolderId();

        QueryWrapper<DocumentFolder> documentFolderQueryWrapper = new QueryWrapper<>();
        documentFolderQueryWrapper.eq("document_id", documentId);
        documentFolderQueryWrapper.eq("folder_id", sourceFolderId);
        // 1. 解除在原先文件夹中的关联
        int rowsDeleted = documentFolderMapper.delete(documentFolderQueryWrapper);
        if (rowsDeleted == 0) {
            throw new BusinessException(ErrorCode.DELETE_FROM_SOURCE_FOLDER_ERROR);
        }

        // 2. 判断目标文件夹是否存在该文档
        Integer existsInTarget = documentFolderMapper.selectCount(documentFolderQueryWrapper);
        if (existsInTarget==null) {
            throw new BusinessException(ErrorCode.DOCUMENT_EXISTS_IN_TARGET_FOLDER);
        }

        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setFolderId(targetFolderId);
        documentFolder.setDocumentId(documentId);
        // 3. 建立新的文件夹与文档的关系
        int rowsInserted = documentFolderMapper.insert(documentFolder);
        if (rowsInserted == 0) {
            throw new BusinessException(ErrorCode.ADD_TO_TARGET_FOLDER_ERROR);
        }

        // 4. 创建操作日志

        Document document = documentMapper.selectById(documentId);

        // 从某个文件移出
        FolderOperationLog operationLog = new FolderOperationLog();
        operationLog.setFolderId(sourceFolderId);
        operationLog.setOperation(MessageConstant.OUT_DOCUMENT);
        operationLog.setUserId(userId);
        operationLog.setDocumentName(document.getName());
        operationLog.setDocumentId(documentId);
        folderOperationLogMapper.insert(operationLog);

        // 移入到某个文件
        operationLog = new FolderOperationLog();
        operationLog.setFolderId(targetFolderId);
        operationLog.setOperation(MessageConstant.CREATE_DOCUMENT);
        operationLog.setUserId(userId);
        operationLog.setDocumentName(document.getName());
        operationLog.setDocumentId(documentId);
        folderOperationLogMapper.insert(operationLog);
        return true;
    }

    @Override
    public List<UserFolderInfoVO> getFolderDocument(Long userId) {
        try {
            // 创建返回的用户文件夹信息列表
            List<UserFolderInfoVO> userFolderInfoList = new ArrayList<>();

            // 创建文件夹查询条件
            LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Folder::getUserId, userId);

            // 查询所有文件夹
            List<Folder> folders = folderMapper.selectList(wrapper);

            // 遍历文件夹列表
            for (Folder folder : folders) {
                // 创建文件夹文档查询条件
                QueryWrapper<DocumentFolder> documentFolderQueryWrapper = new QueryWrapper<>();
                documentFolderQueryWrapper.eq("folder_id", folder.getId());

                // 查询文件夹中的所有文档
                List<DocumentFolder> documentFolders = documentFolderMapper.selectList(documentFolderQueryWrapper);

                // 获取文档ID列表
                List<Long> documentIds = documentFolders.stream()
                        .map(DocumentFolder::getDocumentId)
                        .collect(Collectors.toList());

                // 根据文档ID列表查询文档详细信息
                if (!documentIds.isEmpty()) {
                    List<Document> documents = documentMapper.selectBatchIds(documentIds);

                    // 创建用户文件夹信息VO对象
                    UserFolderInfoVO userFolderInfo = new UserFolderInfoVO();
                    userFolderInfo.setFolderId(folder.getId());
                    userFolderInfo.setUserId(userId);
                    userFolderInfo.setFolderName(folder.getName());
                    userFolderInfo.setPermissions(folder.getPermissions());
                    userFolderInfo.setDocuments(documents);

                    // 添加到返回列表中
                    userFolderInfoList.add(userFolderInfo);
                }
            }

            return userFolderInfoList;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

    }

    @Override
    public boolean moveDocumentToFolder(MoveDocumentToFolderDTO moveDocumentToFolderDTO, Long userId) {
        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setDocumentId(moveDocumentToFolderDTO.getDocumentId());
        documentFolder.setFolderId(moveDocumentToFolderDTO.getFolderId());
        documentFolderMapper.insert(documentFolder);


        // 创建操作日志
        FolderOperationLog operationLog = new FolderOperationLog();
        Document document = documentMapper.selectById(moveDocumentToFolderDTO.getDocumentId());

        operationLog.setFolderId(moveDocumentToFolderDTO.getFolderId());

        operationLog.setOperation(MessageConstant.CREATE_DOCUMENT);

        operationLog.setUserId(userId);

        operationLog.setDocumentName(document.getName());

        operationLog.setDocumentId(moveDocumentToFolderDTO.getDocumentId());

        folderOperationLogMapper.insert(operationLog);

        return true;
    }


}




