package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static hope.smarteditor.common.constant.MessageConstant.DEF;

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
    private RecentDocumentsMapper recentDocumentsMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    @Autowired
    private RecentDocumentsService recentDocumentsService;

    @Autowired
    private FavoriteDocumentMapper favoriteDocumentMapper;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private DeletedInfoMapper deletedInfoMapper;


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

        Folder folder1 = folderMapper.selectById(folderDTO.getId());
        BeanUtils.copyProperties(folderDTO, folder);
        // 该用户不能创建名字为默认文件夹的文件夹
        if(folder.getName().equals("默认文件夹")){
            throw new BusinessException(ErrorCode.FOLDER_ERROR);
        }if(folder1.getName().equals("默认文件夹")){
            throw new BusinessException(ErrorCode.FOLDER_ERROR);
        }
        // 2.创建操作日志
        FolderOperationLog folderOperationLog = new FolderOperationLog();
        folderOperationLog.setFolderId(folderDTO.getId());
        folderOperationLog.setOperation(MessageConstant.UPDATE_FOLDER +"---新名字："+ folderDTO.getName());
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
    public boolean deleteDocument(Long documentId,Long folderId,Long userId)  {
        int i = documentFolderMapper.delete(new QueryWrapper<DocumentFolder>().eq("document_id", documentId).eq("folder_id", folderId));

        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return false;
        }

        // 执行逻辑删除  并且设置删除时间
        documentMapper.deleteById(documentId);

        // 删除最近文档的记录
        LambdaQueryWrapper<RecentDocuments> recentDocumentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recentDocumentsLambdaQueryWrapper.eq(RecentDocuments::getDocumentId, documentId);
        recentDocumentsMapper.delete(recentDocumentsLambdaQueryWrapper);

        // 新增操作日志
        FolderOperationLog operationLog = new FolderOperationLog();
        operationLog.setFolderId(folderId);
        operationLog.setOperation("删除");
        operationLog.setUserId(document.getUserId());
        operationLog.setDocumentName(document.getName());
        operationLog.setDocumentId(documentId);
        folderOperationLogMapper.insert(operationLog);

        //插入回收站
        DeletedInfo deletedInfo = new DeletedInfo();
        deletedInfo.setDocumentId(documentId);
        deletedInfo.setUserId(userId);
        deletedInfo.setOriginalFolderId(folderId);

        deletedInfoMapper.insert(deletedInfo);

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

    private  boolean checkIfDocumentIsFavorited(String userId, Long documentId) {

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
    public List<DocumentInfoVO> getDocumentByFolderId(Long folderId) {
        // 构造查询条件
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Document::getId, documentFolderMapper.getDocumentIdsByFolderId(folderId))
                .orderByDesc(Document::getUpdateTime);
        // 执行查询
        List<Document> documents = documentMapper.selectList(queryWrapper);

        // 转换 Document 为 DocumentInfoVO
        List<DocumentInfoVO> documentInfoVOList = new ArrayList<>();
        for (Document document : documents) {
            DocumentInfoVO documentInfoVO = new DocumentInfoVO();
            BeanUtils.copyProperties(document, documentInfoVO);

            documentInfoVO.setCreateUserNickname(userDubboService.getUserNameByUserId(document.getUserId()));

            documentInfoVO.setIsFavorite(checkIfDocumentIsFavorited(String.valueOf(document.getUserId()), document.getId()));
            // 获取文档的所在文件夹 如果没有则为默认文件夹
            DocumentFolder documentFolder = new DocumentFolder();
            documentFolder.setDocumentId(document.getId());
            LambdaQueryWrapper<DocumentFolder> documentFolderLambdaQueryWrapper = new LambdaQueryWrapper<>();
            documentFolderLambdaQueryWrapper.eq(DocumentFolder::getDocumentId, document.getId());
            DocumentFolder documentFolder1 = documentFolderMapper.selectOne(documentFolderLambdaQueryWrapper);
            if (documentFolder1 != null) {
                documentInfoVO.setOriginalFolder(folderMapper.selectById(documentFolder1.getFolderId()).getName());
            }else documentInfoVO.setOriginalFolder(DEF);

            documentInfoVOList.add(documentInfoVO);
        }

        return documentInfoVOList;
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

    @Override
    public Boolean deleteDocumentByFolderId(DeleteDocumentByFolderIdDTO deleteDocumentByFolderIdDTO,Long userId) {
        // 获取文件夹ID和文档ID列表
        Long folderId = deleteDocumentByFolderIdDTO.getFolderId();
        List<Long> documentIds = deleteDocumentByFolderIdDTO.getDocumentIds();

        // 检查文档ID列表是否为空
        if (documentIds == null || documentIds.isEmpty()) {
            return false;
        }

        // 遍历文档ID列表并执行删除操作
        for (Long documentId : documentIds) {
            // 检查文档是否存在
            Document document = documentMapper.selectById(documentId);
            if (document == null) {
                return false;
            }

            // 删除文件夹中的文档记录
            QueryWrapper<DocumentFolder> documentFolderQueryWrapper = new QueryWrapper<>();
            documentFolderQueryWrapper.eq("folder_id", folderId).eq("document_id", documentId);
            documentFolderMapper.delete(documentFolderQueryWrapper);

            // 执行逻辑删除并设置删除时间
            documentMapper.deleteById(documentId);

            // 删除最近文档的记录
            LambdaQueryWrapper<RecentDocuments> recentDocumentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
            recentDocumentsLambdaQueryWrapper.eq(RecentDocuments::getDocumentId, documentId);
            recentDocumentsMapper.delete(recentDocumentsLambdaQueryWrapper);

            // 新增操作日志
            FolderOperationLog operationLog = new FolderOperationLog();
            operationLog.setFolderId(folderId);
            operationLog.setOperation("删除");
            operationLog.setUserId(document.getUserId());
            operationLog.setDocumentId(documentId);
            operationLog.setDocumentName(document.getName());
            folderOperationLogMapper.insert(operationLog);

            //插入回收站
            DeletedInfo deletedInfo = new DeletedInfo();
            deletedInfo.setDocumentId(documentId);
            deletedInfo.setUserId(userId);
            deletedInfo.setFolderId(folderId);
            deletedInfoMapper.insert(deletedInfo);
        }

        return true;
    }

    @Override
    public Boolean recoverDocument(RecoverDocumentDTO recoverDocumentDTO,Long userId) {
        //  1.首先将文档从回收站中删除 并且逻辑删除改为0
        Document document = new Document();
        document.setId(recoverDocumentDTO.getDocumentId());
        documentMapper.recoverDocument(document.getId());

        //  2.从删除文档中找到该文档的原始文件夹id，将文档恢复到原来的文件夹  并且把删除记录删除
        DeletedInfo deletedInfo = new DeletedInfo();
        LambdaQueryWrapper<DeletedInfo> deletedInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();

        deletedInfoLambdaQueryWrapper.eq(DeletedInfo::getDocumentId, recoverDocumentDTO.getDocumentId());
        DeletedInfo deletedInfo1 = deletedInfoMapper.selectO(recoverDocumentDTO.getDocumentId());
        deletedInfoMapper.delete(deletedInfoLambdaQueryWrapper);
        recoverDocumentDTO.setOriginalFolderId(deletedInfo1.getOriginalFolderId());


        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setDocumentId(recoverDocumentDTO.getDocumentId());
        documentFolder.setFolderId(recoverDocumentDTO.getOriginalFolderId());
        documentFolderMapper.insert(documentFolder);
        //  3.文件夹操作日志进行记录
        FolderOperationLog operationLog = new FolderOperationLog();
        operationLog.setFolderId(recoverDocumentDTO.getOriginalFolderId());
        operationLog.setOperation("恢复");
        operationLog.setUserId(userId);
        operationLog.setDocumentName(document.getName());
        operationLog.setDocumentId(recoverDocumentDTO.getDocumentId());
        documentMapper.selectById(recoverDocumentDTO.getDocumentId()).getName();
        operationLog.setDocumentName(document.getName());
        folderOperationLogMapper.insert(operationLog);

        //  4.更新最近文档
        RecentDocuments recentDocuments = new RecentDocuments();
        recentDocuments.setUserId(userId);
        recentDocuments.setDocumentId(recoverDocumentDTO.getDocumentId());
        recentDocumentsMapper.insert(recentDocuments);
        return true;
    }


}




