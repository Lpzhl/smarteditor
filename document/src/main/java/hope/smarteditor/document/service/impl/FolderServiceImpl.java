package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.constant.AuthorityConstant;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.constant.UserInfoConstant;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.common.model.vo.DocumentInfoVO;
import hope.smarteditor.common.model.vo.UserFolderInfoVO;
import hope.smarteditor.document.annotation.HandleException;
import hope.smarteditor.document.mapper.*;
import hope.smarteditor.document.service.FolderService;
import hope.smarteditor.document.service.RecentDocumentsService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RecentDocumentsService recentDocumentsService;

    @Autowired
    private FavoriteDocumentMapper favoriteDocumentMapper;

    @Autowired
    private RecentDocumentsMapper recentDocumentsMapper;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;


    @Override
    public boolean createFolder(FolderDTO folderDTO) {
        try {

            // 1.创建文件夹
            Folder folder = new Folder();
            BeanUtils.copyProperties(folderDTO,folder);


            folder.setPermissions(AuthorityConstant.VIEW);
            // 该用户不能创建名字为默认文件夹的文件夹
            if(folder.getName().equals(MessageConstant.UPDATE_FAILED)){
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
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
        // 1.删除文件夹 不能删除默认文件夹
        Folder folder = folderMapper.selectById(folderId);
        if(folder.getName().equals("默认文件夹")){
            throw new BusinessException(ErrorCode.FOLDER_ERROR);
        }
        int i = folderMapper.deleteById(folderId);
        return i > 0;
    }

    @Override
    @HandleException
    public boolean updateFolder(FolderUpdateDTO folderDTO,Long userId) {
        // 1.更新文件夹名字
        Folder folder = new Folder();

        BeanUtils.copyProperties(folderDTO, folder);
        // 该用户不能创建名字为默认文件夹的文件夹
        if(folder.getName().equals("默认文件夹")){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

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

        // 新增操作日志
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

    private boolean checkIfDocumentIsFavorited(String userId, Long documentId) {

        QueryWrapper<FavoriteDocument> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", userId).eq("document_id", documentId);

        int count = favoriteDocumentMapper.selectCount(queryWrapper);
        return count > 0;
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
                // 根据文档ID列表查询文档详细信息
                UserFolderInfoVO userFolderInfo = new UserFolderInfoVO();
                userFolderInfo.setFolderId(folder.getId());
                userFolderInfo.setUserId(userId);
                userFolderInfo.setFolderName(folder.getName());
                userFolderInfo.setPermissions(folder.getPermissions());
                userFolderInfo.setDescription(folder.getDescription());
                // 查询文件夹中的所有文档
                List<DocumentFolder> documentFolders = documentFolderMapper.selectList(documentFolderQueryWrapper);

                // 获取文档ID列表
                List<Long> documentIds = documentFolders.stream()
                        .map(DocumentFolder::getDocumentId)
                        .collect(Collectors.toList());
                List<DocumentInfoVO> documentInfoVOList = new ArrayList<>();
                List<Document> documents = new ArrayList<>();
                if (!documentIds.isEmpty()) {
                     documents= documentMapper.selectBatchIds(documentIds);
                     for(Document document :documents){
                         DocumentInfoVO documentInfoVO = new DocumentInfoVO();
                         BeanUtils.copyProperties(document, documentInfoVO);
                         documentInfoVOList.add(documentInfoVO);
                         User userInfo = userDubboService.getUserInfoByUserId(document.getUserId());
                         documentInfoVO.setCreateUserNickname(userInfo.getNickname());
                         boolean isFavorited = checkIfDocumentIsFavorited(String.valueOf(userId), document.getId());
                         documentInfoVO.setIsFavorite(isFavorited);
                     }
                    userFolderInfo.setDocuments(documentInfoVOList);
                }else {
                    userFolderInfo.setDocuments(documentInfoVOList);
                }
                userFolderInfoList.add(userFolderInfo);
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

    @Override
    public List<Folder> searchFoldersByName(String keyword, Long userId) {
        return folderMapper.searchFoldersByName(keyword, userId);
    }

    @Override
    public List<Document> getDocumentByFolderId(Long folderId) {
        // 构造查询条件
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Document::getId, documentFolderMapper.getDocumentIdsByFolderId(folderId))
                .orderByDesc(Document::getUpdateTime);
        // 执行查询
        return documentMapper.selectList(queryWrapper);
    }

    @Override
    public String deleteRecentDocument(Long documentId, Long userId) {
        // 删除最近使用的文档
        QueryWrapper<RecentDocuments> recentDocumentsQueryWrapper = new QueryWrapper<>();
        recentDocumentsQueryWrapper.eq("user_id", userId).eq("document_id", documentId);
        recentDocumentsMapper.delete(recentDocumentsQueryWrapper);

        // 更新 Redis 中的数据
        String key = UserInfoConstant.RECENT_DOCUMENTS_KEY_PREFIX + userId;
        String value = String.valueOf(documentId);
        redisTemplate.opsForZSet().remove(key, value);

        return MessageConstant.SUCCESSFUL;
    }

}




