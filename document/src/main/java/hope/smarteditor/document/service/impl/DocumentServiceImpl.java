package hope.smarteditor.document.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.dto.TemplateDocumentUpdateDTO;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.common.model.vo.*;
import hope.smarteditor.document.config.RedisService;
import hope.smarteditor.document.mapper.*;
import hope.smarteditor.document.service.DocumentService;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static hope.smarteditor.common.constant.MessageConstant.DEF;

/**
* @author LoveF
* @description 针对表【document】的数据库操作Service实现
* @createDate 2024-05-22 14:26:07
*/
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document>
    implements DocumentService{

    @Autowired
    private MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucketName;

    @Autowired
    private  DocumentMapper documentMapper;

    @Resource
    private DocumentpermissionsMapper documentpermissionsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Autowired
    private PermissionsMapper permissionsMapper;

    @Autowired
    private DocumentOperationMapper documentOperationMapper;

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private DocumentFolderMapper documentFolderMapper;

    @Resource
    private FavoriteDocumentMapper favoriteDocumentMapper;

    @Autowired
    private TemplateDocumentMapper templateDocumentMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    @Autowired
    private RecentDocumentsMapper recentDocumentsMapper;

    @Resource
    private FolderOperationLogMapper folderOperationLogMapper;

    @Resource
    private DeletedInfoMapper deletedInfoMapper;

    @Override
    public String uploadFile(MultipartFile file) throws Exception{
        try {
            String objectName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            System.out.println("objectName = " + objectName);
            InputStream inputStream = file.getInputStream();

            // 检查存储桶是否存在，不存在则创建
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                //设置存储桶的访问权限
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            String fullUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            //.expiry(60 * 60 * 24) // URL有效期为24小时
                            .build()
            );
            URL url = new URL(fullUrl);
            String baseUrl = url.getProtocol() + "://" + url.getHost() +":9000"+ url.getPath();

            return baseUrl;
        } catch (MinioException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 保存文件信息
     * @param documentUploadDTO
     * @return
     */
    @Override
    public Document saveDocument(DocumentUploadDTO documentUploadDTO) {
        // 构建文档信息
        if (documentUploadDTO.getContent() == null) {
            documentUploadDTO.setContent("默认文本");
        }

        Document document = new Document();
        document.setUserId(documentUploadDTO.getUserId());
        document.setName(documentUploadDTO.getName() == null ? "未命名文档" : documentUploadDTO.getName());
        document.setContent(documentUploadDTO.getContent());
        document.setSummary(documentUploadDTO.getSummary());
        document.setType(documentUploadDTO.getType());
        document.setLabel(documentUploadDTO.getLabel());
        document.setStatus(documentUploadDTO.getStatus());
        document.setCategory(documentUploadDTO.getCategory());
        document.setSubject(documentUploadDTO.getSubject());
        document.setCreateTime(new Date());
        document.setUpdateTime(new Date());

        // 插入文档到数据库
        int insert = documentMapper.insert(document);
        if (insert == 0) {
            throw new BusinessException(ErrorCode.SAVE_FILE_ERROR);
        }

        // 获取插入后的文档
        Document savedDocument = documentMapper.selectById(document.getId());

        // 设置文档权限
        setDocumentPermissions(savedDocument);

        // 保存文档版本
        saveDocumentVersion(savedDocument);

        // 更新缓存
        try {
            updateDocumentCache(savedDocument.getUserId(), savedDocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 保存到用户选择的文件夹或默认文件夹
        saveToFolder(documentUploadDTO, savedDocument);

        // 添加操作日志
        addFolderOperationLog(savedDocument);

        return savedDocument;
    }

    private void setDocumentPermissions(Document document) {
        Documentpermissions documentpermissions = new Documentpermissions();
        documentpermissions.setDocumentId(document.getId());
        documentpermissions.setUserId(document.getUserId());
        documentpermissions.setPermissionId(1L); // 设置为创建者，权限为可编辑
        documentpermissionsMapper.insert(documentpermissions);
    }

    private void saveDocumentVersion(Document document) {
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentId(document.getId());
        documentVersion.setVersion(getNextVersionNumber(document.getId()));
        documentVersion.setContent(document.getContent());
        documentVersion.setSummary(document.getSummary());
        documentVersion.setUsername(userDubboService.getUserNameByUserId(document.getUserId()));
        documentVersion.setUpdateTime(new Date());
        documentVersionMapper.insert(documentVersion);
    }

    private void saveToFolder(DocumentUploadDTO documentUploadDTO, Document document) {
        Long folderId;
        if (documentUploadDTO.getFolderId() != null) {
            // 用户选择了文件夹，使用用户选择的文件夹
            folderId = documentUploadDTO.getFolderId();
        } else {
            // 用户没有选择文件夹，默认使用默认文件夹
            folderId = getDefaultFolderId(document.getUserId());
        }

        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setDocumentId(document.getId());
        documentFolder.setFolderId(folderId);
        documentFolderMapper.insert(documentFolder);
    }

    private Long getDefaultFolderId(Long userId) {
        LambdaQueryWrapper<Folder> folderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        folderLambdaQueryWrapper.eq(Folder::getUserId, userId);
        folderLambdaQueryWrapper.eq(Folder::getName, DEF);
        Folder folder = folderMapper.selectOne(folderLambdaQueryWrapper);

        if (folder == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return folder.getId();
    }

    private void addFolderOperationLog(Document document) {
        FolderOperationLog folderOperationLog = new FolderOperationLog();
        folderOperationLog.setUserId(document.getUserId());
        folderOperationLog.setFolderId(getDefaultFolderId(document.getUserId()));
        folderOperationLog.setDocumentId(document.getId());
        folderOperationLog.setOperation("插入");
        folderOperationLog.setDocumentName(document.getName());
        folderOperationLogMapper.insert(folderOperationLog);
    }

 /*   @Override
    public Document saveDocument(DocumentUploadDTO documentUploadDTO) {
        // 构建文档信息
        if(documentUploadDTO.getContent() == null){
            documentUploadDTO.setContent("默认文本");
        }

        Document document = new Document();
        document.setUserId(documentUploadDTO.getUserId());
        document.setName(documentUploadDTO.getName() == null ? "未命名文档" : documentUploadDTO.getName());
        document.setContent(documentUploadDTO.getContent());
        document.setSummary(documentUploadDTO.getSummary());
        document.setType(documentUploadDTO.getType());
        document.setLabel(documentUploadDTO.getLabel());
        document.setStatus(documentUploadDTO.getStatus());
        document.setCategory(documentUploadDTO.getCategory());
        document.setSubject(documentUploadDTO.getSubject());
        document.setCreateTime(new Date());
        document.setUpdateTime(new Date());

        // 插入文档到数据库
        int insert = documentMapper.insert(document);
        if (insert == 0) {
            throw new BusinessException(ErrorCode.SAVE_FILE_ERROR);
        }

        Document savedDocument = documentMapper.selectById(document.getId());

        // 设置文档权限
        Documentpermissions documentpermissions = new Documentpermissions();
        documentpermissions.setDocumentId(document.getId());
        documentpermissions.setUserId(document.getUserId());
        documentpermissions.setPermissionId(1L); // 设置为创建者，权限为可编辑
        documentpermissionsMapper.insert(documentpermissions);

        // 保存文档版本
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentId(document.getId());
        documentVersion.setVersion(getNextVersionNumber(document.getId()));
        documentVersion.setContent(document.getContent());
        documentVersion.setSummary(document.getSummary());
        documentVersion.setUsername(userDubboService.getUserNameByUserId(document.getUserId()));
        documentVersion.setUpdateTime(new Date());
        documentVersionMapper.insert(documentVersion);

        // 更新缓存
        try {
            updateDocumentCache(document.getUserId(), savedDocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // todo 如果用户选择了文件夹documentUploadDTO.getFolderId()那就保存到相应的文件夹中   如果用户没有选择文件夹，则默认添加到默认文件夹中

        // 搜索该用户的默认文件夹并添加到默认文件夹中
        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setDocumentId(document.getId());
        LambdaQueryWrapper<Folder> folderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        folderLambdaQueryWrapper.eq(Folder::getUserId, document.getUserId());
        folderLambdaQueryWrapper.eq(Folder::getName, DEF);
        Folder folder = folderMapper.selectOne(folderLambdaQueryWrapper);
        documentFolder.setFolderId(folder.getId());
        documentFolderMapper.insert(documentFolder);

        // 添加日志
        FolderOperationLog folderOperationLog = new FolderOperationLog();
        folderOperationLog.setUserId(document.getUserId());
        folderOperationLog.setFolderId(folder.getId());
        folderOperationLog.setDocumentId(document.getId());
        folderOperationLog.setOperation("插入");
        folderOperationLog.setDocumentName(document.getName());
        folderOperationLogMapper.insert(folderOperationLog);

        return savedDocument;
    }*/

    private void updateDocumentCache(Long userId, Document document) throws Exception {
        String cacheKey = "documents:user:" + userId;

        // 获取当前缓存的数据
        List<Object> cachedDocuments = (List<Object>) redisTemplate.opsForValue().get(cacheKey);
        ObjectMapper objectMapper = new ObjectMapper();
        List<DocumentInfoVO> documents = new ArrayList<>();

        if (cachedDocuments != null) {
            documents = cachedDocuments.stream()
                    .map(obj -> objectMapper.convertValue(obj, DocumentInfoVO.class))
                    .collect(Collectors.toList());
        }

        // 构建新的 DocumentInfoVO
        DocumentInfoVO newDocumentInfoVO = new DocumentInfoVO();
        BeanUtils.copyProperties(document, newDocumentInfoVO);
        newDocumentInfoVO.setCreateUserNickname(userDubboService.getUserInfoByUserId(document.getUserId()).getNickname());
        newDocumentInfoVO.setIsFavorite(checkIfDocumentIsFavorited(String.valueOf(userId), document.getId()));

        // 获取文档的所在文件夹 如果没有则为默认文件夹
        LambdaQueryWrapper<DocumentFolder> documentFolderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        documentFolderLambdaQueryWrapper.eq(DocumentFolder::getDocumentId, document.getId());
        DocumentFolder documentFolder = documentFolderMapper.selectOne(documentFolderLambdaQueryWrapper);
        if (documentFolder != null) {
            String name = folderMapper.selectById(documentFolder.getFolderId()).getName();
            newDocumentInfoVO.setOriginalFolder(name);
        } else {
            newDocumentInfoVO.setOriginalFolder(DEF);
        }

        // 将新的 DocumentInfoVO 添加到缓存列表中
        documents.add(newDocumentInfoVO);

        // 更新缓存
        redisTemplate.opsForValue().set(cacheKey, documents);
        redisTemplate.expire(cacheKey, 7, TimeUnit.HOURS);
    }




    /**
     * 更新文件信息
     * @param documentId
     * @param documentUpdateDTO
     * @return
     */
    @Override
    public Document updateDocument(Long documentId, DocumentUpdateDTO documentUpdateDTO) {
        Long userId = documentUpdateDTO.getUserId();
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return null;
        }
        String name = userDubboService.getUserNameByUserId(userId);
        // 保存旧版本到文档版本表
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentId(document.getId());
        documentVersion.setVersion(getNextVersionNumber(documentId));
        documentVersion.setContent(document.getContent());
        documentVersion.setSummary(document.getSummary());
        documentVersion.setUpdateTime(new Date());

        documentVersion.setUsername(name);
        documentVersionMapper.insert(documentVersion);

        // 更新文档对象中非空字段
        if (documentUpdateDTO.getName() != null) {
            document.setName(documentUpdateDTO.getName());
        }
        if (documentUpdateDTO.getContent() != null) {
            document.setContent(documentUpdateDTO.getContent());
        }
        if (documentUpdateDTO.getSummary() != null) {
            document.setSummary(documentUpdateDTO.getSummary());
        }
        if (documentUpdateDTO.getType() != null) {
            document.setType(documentUpdateDTO.getType());
        }
        if (documentUpdateDTO.getLabel() != null) {
            document.setLabel(documentUpdateDTO.getLabel());
        }
        if (documentUpdateDTO.getSubject() != null) {
            document.setSubject(documentUpdateDTO.getSubject());
        }
        if (documentUpdateDTO.getCategory() != null) {
            document.setCategory(documentUpdateDTO.getCategory());
        }

        document.setUpdateTime(new Date());
        documentMapper.updateById(document);

        // 更新cacheKey1
        String cacheKey1 = "user:" + document.getUserId() + ":documents";
        List<Object> userDocuments = (List<Object>) redisTemplate.opsForValue().get(cacheKey1);

        if (userDocuments != null) {
            List<Document> documentList = userDocuments.stream()
                    .map(doc -> objectMapper.convertValue(doc, Document.class))
                    .collect(Collectors.toList());
            // 更新文档列表中的文档信息
            for (int i = 0; i < documentList.size(); i++) {
                if (documentList.get(i).getId().equals(documentId)) {
                    documentList.set(i, document);
                    break;
                }
            }

            redisTemplate.opsForValue().set(cacheKey1, documentList);
            redisTemplate.expire(cacheKey1, 7, TimeUnit.DAYS);
        }

        // 更新Redis中缓存的信息
        String cacheKey = "document:" + documentId;
        redisTemplate.opsForValue().set(cacheKey, document);
        redisTemplate.expire(cacheKey, 7, TimeUnit.HOURS);

        return document;
    }

    private double getNextVersionNumber(Long documentId) {
        // 使用QueryWrapper获取文档版本表中该文档的最大版本号
        QueryWrapper<DocumentVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId)
                .orderByDesc("version")
                .last("LIMIT 1");

        DocumentVersion documentVersion = documentVersionMapper.selectOne(wrapper);
        Double maxVersion = null;

        //如果documentVersion 为null 则版本号为1.00，否则版本号加0.01
        if (documentVersion != null) {
            maxVersion = documentVersion.getVersion();
        }

        // 如果文档版本表中没有记录，则版本号为1.00，否则版本号加0.01
        return (maxVersion == null) ? 1.00 : Math.round((maxVersion + 0.01) * 100.0) / 100.0;
    }





    @Override
    @Transactional
    public boolean deleteDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return false;
        }

        // 获取文档所在文件夹的ID
        Long folderId =documentFolderMapper.selectOne(new QueryWrapper<DocumentFolder>().eq("document_id", documentId)).getFolderId();
        Long userId = document.getUserId();

        // 删除文档文件夹关联记录
        documentFolderMapper.delete(new QueryWrapper<DocumentFolder>().eq("document_id", documentId).eq("folder_id", folderId));

        // 执行逻辑删除 并且设置删除时间
        int i = documentMapper.deleteById(documentId);

        // 删除最近文档的记录
        LambdaQueryWrapper<RecentDocuments> recentDocumentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recentDocumentsLambdaQueryWrapper.eq(RecentDocuments::getDocumentId, documentId);
        recentDocumentsMapper.delete(recentDocumentsLambdaQueryWrapper);
        try {
            removeDocumentFromCache(userId,documentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (i > 0) {
            // 新增操作日志
            FolderOperationLog operationLog = new FolderOperationLog();
            operationLog.setFolderId(folderId);
            operationLog.setOperation("删除");
            operationLog.setUserId(userId);
            operationLog.setDocumentName(document.getName());
            operationLog.setDocumentId(documentId);
            folderOperationLogMapper.insert(operationLog);

            // 插入回收站
            DeletedInfo deletedInfo = new DeletedInfo();
            deletedInfo.setDocumentId(documentId);
            deletedInfo.setUserId(userId);
            deletedInfo.setOriginalFolderId(folderId);
            deletedInfoMapper.insert(deletedInfo);

            // 删除cacheKey1中的文档信息
            String cacheKey1 = "user:" + userId + ":documents";
            List<Object> userDocumentsRaw = (List<Object>) redisTemplate.opsForValue().get(cacheKey1);
            if (userDocumentsRaw != null) {
                List<Document> userDocuments = userDocumentsRaw.stream()
                        .map(doc -> objectMapper.convertValue(doc, Document.class))
                        .filter(doc -> !doc.getId().equals(documentId))
                        .collect(Collectors.toList());
                redisTemplate.opsForValue().set(cacheKey1, userDocuments);
                redisTemplate.expire(cacheKey1, 7, TimeUnit.DAYS);
            }

            // 删除缓存的信息
            String cacheKey = "document:" + documentId;
            redisTemplate.delete(cacheKey);
        }

        return i > 0;
    }




    @Override
    public void setDocumentVisibility(Long documentId) {
        // 1. 查询文档对象
        QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", documentId);
        Document document = documentMapper.selectOne(queryWrapper);

        // 2. 获取当前文档的可见性状态
        Integer visibility = document.getVisibility();

        // 3. 根据当前可见性状态，进行设置
        if (visibility == 1) {
            // 如果当前文档是公开的，则将其设置为私有
            document.setVisibility(0);
        } else if (visibility == 0) {
            // 如果当前文档是私有的，则将其设置为公开
            document.setVisibility(1);
        }

        // 4. 更新文档对象
        documentMapper.updateById(document);

        // 5. 同步更新缓存信息
        String cacheKey = "document:" + documentId;
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        if (hasKey != null && hasKey) {
            // 更新缓存中的文档信息
            Document updatedDocument = documentMapper.selectById(documentId);
            redisTemplate.opsForValue().set(cacheKey, updatedDocument);
            // 设置缓存过期时间，例如7天
            redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);
        }
    }

    /**
     * 获取逻辑删除的文档
     * @param userId
     * @return
     */
    @Override
    public List<DocumentInfoVO> getDeletedDocuments(Long userId) {
        // 获取逻辑删除的文档列表
        List<Document> deletedDocuments = documentMapper.getDeletedDocuments(userId);

        if (deletedDocuments.isEmpty()) {
            return Collections.emptyList();  // 如果没有删除的文档，返回空列表
        }

        // 获取文档 ID 列表
        Set<Long> documentIds = deletedDocuments.stream()
                .map(Document::getId)
                .collect(Collectors.toSet());

        // 批量查询删除信息
        Map<Long, DeletedInfo> deletedInfoMap = deletedInfoMapper.selectList(
                new LambdaQueryWrapper<DeletedInfo>().in(DeletedInfo::getDocumentId, documentIds)
        ).stream().collect(Collectors.toMap(DeletedInfo::getDocumentId, deletedInfo -> deletedInfo));

        // 批量查询用户信息
        Map<Long, User> userMap = deletedDocuments.stream()
                .map(Document::getUserId)
                .distinct()
                .collect(Collectors.toMap(
                        userId1 -> userId1,
                        userDubboService::getUserInfoByUserId
                ));

        // 批量查询文件夹信息
        Map<Long, String> folderMap = documentFolderMapper.selectList(
                new LambdaQueryWrapper<DocumentFolder>().in(DocumentFolder::getDocumentId, documentIds)
        ).stream().collect(Collectors.toMap(
                DocumentFolder::getDocumentId,
                df -> folderMapper.selectById(df.getFolderId()).getName()
        ));

        // 转换为 DocumentInfoVO
        return deletedDocuments.stream().map(document -> {
            DocumentInfoVO documentInfoVO = new DocumentInfoVO();
            BeanUtils.copyProperties(document, documentInfoVO);

            // 设置删除时间
            DeletedInfo deletedInfo = deletedInfoMap.get(document.getId());
            if (deletedInfo != null) {
                documentInfoVO.setUpdateTime(deletedInfo.getDeletionTime());
            }

            // 设置用户昵称
            User userInfo = userMap.get(document.getUserId());
            if (userInfo != null) {
                documentInfoVO.setCreateUserNickname(userInfo.getNickname());
            }

            // 设置是否收藏
            boolean isFavorited = checkIfDocumentIsFavorited(String.valueOf(userId), document.getId());
            documentInfoVO.setIsFavorite(isFavorited);

            // 设置文件夹名称
            String folderName = folderMap.get(document.getId());
            documentInfoVO.setOriginalFolder(folderName != null ? folderName : DEF);

            return documentInfoVO;
        }).collect(Collectors.toList());
    }

    private DocumentInfoVO convertToDocumentInfoVO(Document document, Long userId) {
        DocumentInfoVO documentInfoVO = new DocumentInfoVO();
        BeanUtils.copyProperties(document, documentInfoVO);
        LambdaQueryWrapper<DeletedInfo> deletedInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deletedInfoLambdaQueryWrapper.eq(DeletedInfo::getDocumentId, document.getId());
        DeletedInfo deletedInfo = deletedInfoMapper.selectOne(deletedInfoLambdaQueryWrapper);
        if(deletedInfo != null) {
            documentInfoVO.setUpdateTime(deletedInfo.getDeletionTime());
        }
        User userInfo = userDubboService.getUserInfoByUserId(document.getUserId());
        documentInfoVO.setCreateUserNickname(userInfo.getNickname());
        boolean isFavorited = checkIfDocumentIsFavorited(String.valueOf(userId), document.getId());
        documentInfoVO.setIsFavorite(isFavorited);
        // 获取文档的所在文件夹 如果没有则为默认文件夹
        LambdaQueryWrapper<DocumentFolder> documentFolderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        documentFolderLambdaQueryWrapper.eq(DocumentFolder::getDocumentId, document.getId());
        DocumentFolder documentFolder = documentFolderMapper.selectOne(documentFolderLambdaQueryWrapper);
        if (documentFolder != null) {
            documentInfoVO.setOriginalFolder(folderMapper.selectById(documentFolder.getFolderId()).getName());
        } else {
            documentInfoVO.setOriginalFolder(DEF);
        }

        return documentInfoVO;
    }


    @Override
    public List<DocumentUserPermisssVO> getParticipants(Long documentId) {
        // 查询文档对象
        QueryWrapper<Documentpermissions> documentpermissionsQueryWrapper = new QueryWrapper<>();
        documentpermissionsQueryWrapper.eq("document_id", documentId);
        List<Documentpermissions> documentpermissions = documentpermissionsMapper.selectList(documentpermissionsQueryWrapper);

        // 获取用户的ID列表
        List<Long> userIds = documentpermissions.stream()
                .map(Documentpermissions::getUserId)
                .collect(Collectors.toList());

        // 查询用户信息
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> users = userDubboService.getUserInfoByUserId(userIds);

        // 获取权限信息
        List<Long> permissionIds = documentpermissions.stream()
                .map(Documentpermissions::getPermissionId)
                .collect(Collectors.toList());
        List<Permissions> permissions = permissionsMapper.selectBatchIds(permissionIds);

        // 构建权限ID到权限名称的映射
        Map<Long, String> permissionIdToNameMap = permissions.stream()
                .collect(Collectors.toMap(Permissions::getPermissionId, Permissions::getPermissionName));

        // 构建用户ID到用户对象的映射
        Map<Long, User> userIdToUserMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 包装成DocumentUserPermisssVO返回给前端
        List<DocumentUserPermisssVO> result = documentpermissions.stream()
                .map(dp -> {
                    DocumentUserPermisssVO vo = new DocumentUserPermisssVO();
                    vo.setUser(userIdToUserMap.get(dp.getUserId()));
                    vo.setPermission(permissionIdToNameMap.get(dp.getPermissionId()));
                    return vo;
                })
                .collect(Collectors.toList());

        return result;
    }
    @Override
    public List<Document> searchDocumentsByName(String keyword, Long userId) {
        return documentMapper.searchDocumentsByName(keyword, userId);
    }

    @Override
    public Document getDocumentById(Long userId, Long documentId) {
        // 获取默认文档文件夹
        QueryWrapper<Folder> folderQueryWrapper = new QueryWrapper<>();
        folderQueryWrapper.eq("user_id", userId).eq("name", "默认文档");
        Folder defaultFolder = folderMapper.selectOne(folderQueryWrapper);

        // 检查该文档是否已经存在于默认文档文件夹中
        QueryWrapper<DocumentFolder> documentFolderQueryWrapper = new QueryWrapper<>();
        documentFolderQueryWrapper.eq("folder_id", defaultFolder.getId()).eq("document_id", documentId);
        DocumentFolder existingDocumentFolder = documentFolderMapper.selectOne(documentFolderQueryWrapper);

        // 如果文档不存在于默认文档文件夹中，则保存
        if (existingDocumentFolder == null) {
            DocumentFolder documentFolder = new DocumentFolder();
            documentFolder.setFolderId(defaultFolder.getId());
            documentFolder.setDocumentId(documentId);
            documentFolderMapper.insert(documentFolder);
        }

        // 返回文档
        return documentMapper.selectById(documentId);
    }

    @Override
    public List<Folder> searchDocumentsByCreator(String keyword, Long userId) {
        return folderMapper.searchFoldersByName(keyword, userId);
    }

        @Override
        public void setDocumentAsTemplate(Long documentId,Long userId)  {
            QueryWrapper<Document> documentQueryWrapper = new QueryWrapper<>();
            documentQueryWrapper.eq("id", documentId).eq("user_id", userId);
            Document document = documentMapper.selectOne(documentQueryWrapper);

            TemplateDocument templateDocument = new TemplateDocument();
            // 将文档设置为模板
            BeanUtils.copyProperties(document,templateDocument);
            templateDocument.setId(null);
            templateDocument.setUserId(userId);
            templateDocumentMapper.insert(templateDocument);

    }

    @Override
    public List<DocumentShareInVO> getDocumentShare(Long userId) {
        // 使用recent_docs缓存键
        String recentDocsCacheKey = "recent_docs:" + userId;
        String recentDocsInfoVOKey = "recent_docsInfoVO:" + userId;

        List<DocumentShareInVO> documentShareInVOList = new ArrayList<>();

        // 从 `recent_docsInfoVO` 缓存中获取缓存的 DocumentInfoVO 列表
        String cachedDocumentInfoVOsJson = (String) redisTemplate.opsForValue().get(recentDocsInfoVOKey);
        List<DocumentInfoVO> documentInfoVOs = new ArrayList<>();

        if (cachedDocumentInfoVOsJson != null) {
            try {
                documentInfoVOs = deserializeDocumentInfoVOList(cachedDocumentInfoVOsJson);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!documentInfoVOs.isEmpty()) {
            // 使用Map分类文档
            Map<Boolean, List<DocumentInfoVO>> categorizedDocuments = documentInfoVOs.stream()
                    .collect(Collectors.partitioningBy(doc -> doc.getUserId().equals(userId)));

            // 创建DocumentShareInVO对象
            DocumentShareInVO sentDocumentShareInVO = new DocumentShareInVO();
            sentDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(true, Collections.emptyList()));
            sentDocumentShareInVO.setCategory("我的分享");

            DocumentShareInVO receivedDocumentShareInVO = new DocumentShareInVO();
            receivedDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(false, Collections.emptyList()));
            receivedDocumentShareInVO.setCategory("我的接收");

            // 将两个对象添加到列表中
            documentShareInVOList.add(sentDocumentShareInVO);
            documentShareInVOList.add(receivedDocumentShareInVO);
        } else {
            synchronized (this) {  // 加锁防止缓存击穿
                // 再次检查缓存
                cachedDocumentInfoVOsJson = (String) redisTemplate.opsForValue().get(recentDocsInfoVOKey);
                if (cachedDocumentInfoVOsJson == null || cachedDocumentInfoVOsJson.isEmpty()) {
                    // 获取用户的所有最近文档
                    List<RecentDocuments> recentDocuments = recentDocumentsMapper.selectList(
                            new QueryWrapper<RecentDocuments>().eq("user_id", userId));

                    if (recentDocuments.isEmpty()) {
                        return Collections.emptyList(); // 如果没有最近文档，则直接返回空列表
                    }

                    List<Long> documentIds = recentDocuments.stream()
                            .map(RecentDocuments::getDocumentId)
                            .collect(Collectors.toList());

                    // 根据documentId获取文档信息
                    List<Document> documents = documentMapper.selectList(
                            new QueryWrapper<Document>().in("id", documentIds));

                    // 对 documentInfoVOs 列表按照 updateTime 进行排序
                    documentInfoVOs.sort(Comparator.comparing(DocumentInfoVO::getUpdateTime).reversed());

                    // 将获取到的文档信息转换为DocumentInfoVO对象
                    documentInfoVOs = documents.stream()
                            .map(doc -> convertToDocumentInfoVO(doc, userId))
                            .collect(Collectors.toList());

                    // 序列化并缓存 DocumentInfoVO 列表
                    String updatedDocumentInfoVOsJson = null;
                    try {
                        updatedDocumentInfoVOsJson = serializeDocumentInfoVOList(documentInfoVOs);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    redisTemplate.opsForValue().set(recentDocsInfoVOKey, updatedDocumentInfoVOsJson, 7, TimeUnit.HOURS);

                    // 使用Map分类文档
                    Map<Boolean, List<DocumentInfoVO>> categorizedDocuments = documentInfoVOs.stream()
                            .collect(Collectors.partitioningBy(doc -> doc.getUserId().equals(userId)));

                    // 创建DocumentShareInVO对象
                    DocumentShareInVO sentDocumentShareInVO = new DocumentShareInVO();
                    sentDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(true, Collections.emptyList()));
                    sentDocumentShareInVO.setCategory("我的分享");

                    DocumentShareInVO receivedDocumentShareInVO = new DocumentShareInVO();
                    receivedDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(false, Collections.emptyList()));
                    receivedDocumentShareInVO.setCategory("我的接收");

                    // 将两个对象添加到列表中
                    documentShareInVOList.add(sentDocumentShareInVO);
                    documentShareInVOList.add(receivedDocumentShareInVO);
                }
            }
        }


        return documentShareInVOList;
    }


/*    @Override
    public List<DocumentShareInVO> getDocumentShare(Long userId) {
        // 使用recent_docs缓存键
        String recentDocsCacheKey = "recent_docs:" + userId;
        List<Object> documentIdsObj = redisTemplate.opsForList().range(recentDocsCacheKey, 0, -1);

        List<DocumentShareInVO> documentShareInVOList = new ArrayList<>();

        if (documentIdsObj != null && !documentIdsObj.isEmpty()) {
            // 将Object类型的ID列表转换为String类型
            List<String> documentIdsStr = documentIdsObj.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // 将字符串形式的ID转换为Long类型
            List<Long> documentIds = documentIdsStr.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            // 根据documentId获取文档信息
            List<Document> documents = documentMapper.selectList(new QueryWrapper<Document>().in("id", documentIds));

            // 使用Map分类文档
            Map<Boolean, List<Document>> categorizedDocuments = documents.stream()
                    .collect(Collectors.partitioningBy(doc -> doc.getUserId().equals(userId)));

            // 创建DocumentShareInVO对象
            DocumentShareInVO sentDocumentShareInVO = new DocumentShareInVO();
            sentDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(true, Collections.emptyList()).stream()
                    .map(doc -> convertToDocumentInfoVO(doc, userId))
                    .collect(Collectors.toList()));
            sentDocumentShareInVO.setCategory("我的分享");

            DocumentShareInVO receivedDocumentShareInVO = new DocumentShareInVO();
            receivedDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(false, Collections.emptyList()).stream()
                    .map(doc -> convertToDocumentInfoVO(doc, userId))
                    .collect(Collectors.toList()));
            receivedDocumentShareInVO.setCategory("我的接收");

            // 将两个对象添加到列表中
            documentShareInVOList.add(sentDocumentShareInVO);
            documentShareInVOList.add(receivedDocumentShareInVO);
        } else {
            synchronized (this) {  // 加锁防止缓存击穿
                documentIdsObj = redisTemplate.opsForList().range(recentDocsCacheKey, 0, -1);
                if (documentIdsObj == null || documentIdsObj.isEmpty()) {
                    // 获取用户的所有最近文档
                    List<RecentDocuments> recentDocuments = recentDocumentsMapper.selectList(
                            new QueryWrapper<RecentDocuments>().eq("user_id", userId));

                    if (recentDocuments.isEmpty()) {
                        return Collections.emptyList(); // 如果没有最近文档，则直接返回空列表
                    }

                    List<Long> documentIds = recentDocuments.stream()
                            .map(RecentDocuments::getDocumentId)
                            .collect(Collectors.toList());

                    // 将Long类型的ID列表转换为字符串类型并更新Redis缓存
                    List<String> documentIdsStr = documentIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.toList());
                    redisTemplate.opsForList().rightPushAll(recentDocsCacheKey, documentIdsStr);
                    redisTemplate.expire(recentDocsCacheKey, 7, TimeUnit.HOURS);

                    // 根据documentId获取文档信息
                    List<Document> documents = documentMapper.selectList(
                            new QueryWrapper<Document>().in("id", documentIds));

                    // 使用Map分类文档
                    Map<Boolean, List<Document>> categorizedDocuments = documents.stream()
                            .collect(Collectors.partitioningBy(doc -> doc.getUserId().equals(userId)));

                    // 创建DocumentShareInVO对象
                    DocumentShareInVO sentDocumentShareInVO = new DocumentShareInVO();
                    sentDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(true, Collections.emptyList()).stream()
                            .map(doc -> convertToDocumentInfoVO(doc, userId))
                            .collect(Collectors.toList()));
                    sentDocumentShareInVO.setCategory("我的分享");

                    DocumentShareInVO receivedDocumentShareInVO = new DocumentShareInVO();
                    receivedDocumentShareInVO.setDocuments(categorizedDocuments.getOrDefault(false, Collections.emptyList()).stream()
                            .map(doc -> convertToDocumentInfoVO(doc, userId))
                            .collect(Collectors.toList()));
                    receivedDocumentShareInVO.setCategory("我的接收");

                    // 将两个对象添加到列表中
                    documentShareInVOList.add(sentDocumentShareInVO);
                    documentShareInVOList.add(receivedDocumentShareInVO);
                }
            }
        }

        return documentShareInVOList;
    }*/






    private  boolean checkIfDocumentIsFavorited(String userId, Long documentId) {

        QueryWrapper<FavoriteDocument> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", userId).eq("document_id", documentId);

        int count = favoriteDocumentMapper.selectCount(queryWrapper);
        return count > 0;
    }
    @Override
    public SearchVO searchDocumentsByContent(String keyword, Long userId) {
        SearchVO searchVO = new SearchVO();
        LambdaQueryWrapper<Document> documentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        documentLambdaQueryWrapper.like(Document::getContent, keyword).eq(Document::getUserId, userId);
        List<Document> documents = documentMapper.selectList(documentLambdaQueryWrapper);
        searchVO.setDocuments(documents);
        return searchVO;
    }

    @Override
    @Transactional
    public void deleteDocumentBatch(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return;
        }

        // 获取所有待删除文档的详细信息
        List<Document> documents = documentMapper.selectBatchIds(documentIds);
        if (documents == null || documents.isEmpty()) {
            return;
        }

        // 记录每个文档对应的原文件夹id和用户id
        Map<Long, Long> documentFolderMap = new HashMap<>();
        Map<Long, Long> documentUserMap = new HashMap<>();
        for (Document document : documents) {
            // 获取文档位置
            documentFolderMap.put(document.getId(),documentFolderMapper.selectOne(new QueryWrapper<DocumentFolder>().eq("document_id", document.getId())).getFolderId());
            documentUserMap.put(document.getId(), document.getUserId());
            // 删除相关缓存
            redisTemplate.delete("document:" + document.getId());
        }

        // 删除文档文件夹关联记录
        documentFolderMapper.delete(new QueryWrapper<DocumentFolder>().in("document_id", documentIds));

        // 执行逻辑删除，并设置删除时间
        documentMapper.deleteBatchIds(documentIds);

        // 删除最近文档的记录
        LambdaQueryWrapper<RecentDocuments> recentDocumentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recentDocumentsLambdaQueryWrapper.in(RecentDocuments::getDocumentId, documentIds);
        recentDocumentsMapper.delete(recentDocumentsLambdaQueryWrapper);



        // 新增操作日志并插入回收站
        for (Long documentId : documentIds) {
            Long folderId = documentFolderMap.get(documentId);
            Long userId = documentUserMap.get(documentId);
            Document document = documents.stream().filter(doc -> doc.getId().equals(documentId)).findFirst().orElse(null);

            if (document != null) {
                // 新增操作日志
                FolderOperationLog operationLog = new FolderOperationLog();
                operationLog.setFolderId(folderId);
                operationLog.setOperation("删除");
                operationLog.setUserId(userId);
                operationLog.setDocumentName(document.getName());
                operationLog.setDocumentId(documentId);
                folderOperationLogMapper.insert(operationLog);

                // 插入回收站
                DeletedInfo deletedInfo = new DeletedInfo();
                deletedInfo.setDocumentId(documentId);
                deletedInfo.setUserId(userId);
                deletedInfo.setOriginalFolderId(folderId);
                deletedInfoMapper.insert(deletedInfo);
            }
        }
    }


    @Override
    public void renameDocument(Long documentId, String newName,Long userId)  {
        Document document = new Document();
        document.setId(documentId);
        document.setName(newName);

        renameDocumentNameLog(documentId, newName, userId);

        documentMapper.updateById(document);
    }

    @Override
    public List<TemplateDocument> getTemplateDocument(Long userId) {
        return templateDocumentMapper.selectList(new QueryWrapper<TemplateDocument>().eq("user_id", userId));
    }

    @Override
    public List<TemplateDocument> getTemplateShow() {
        QueryWrapper<TemplateDocument> queryWrapper = new QueryWrapper<>();
        queryWrapper.last("LIMIT 10");
        return templateDocumentMapper.selectList(queryWrapper);
    }

    @Override
    public void editTemplate(TemplateDocumentUpdateDTO templateDocument) {
        TemplateDocument templateDocument1 = new TemplateDocument();
        BeanUtils.copyProperties(templateDocument, templateDocument1);
        templateDocumentMapper.updateById(templateDocument1);
    }

    @Override
    public void saveLog(Long documentId,DocumentUpdateDTO documentUpdateDTO) {
        String name =  userDubboService.getUserNameByUserId(documentUpdateDTO.getUserId());
        // 记录用户操作到文档操作表
        DocumentOperation documentOperation = new DocumentOperation();
        documentOperation.setDocumentId(documentId);
        documentOperation.setUserId(documentUpdateDTO.getUserId());
        documentOperation.setOperation("编辑");
        documentOperation.setDescription("用户 " + name + " 更新了文档《 " + documentMapper.selectById(documentId).getName()+ "》");
        documentOperation.setOperationTime(new Date());
        documentOperationMapper.insert(documentOperation);
    }

    @Override
    public void createLog(Long documentId,Long userId)  {
        String name =  userDubboService.getUserNameByUserId(userId);
        // 记录用户操作到文档操作表
        DocumentOperation documentOperation = new DocumentOperation();
        documentOperation.setDocumentId(documentId);
        documentOperation.setUserId(userId);
        documentOperation.setOperation("创建");
        documentOperation.setDescription("用户 " + name + " 创建了文档《 " + documentMapper.selectById(documentId).getName()+ "》");
        documentOperation.setOperationTime(new Date());
        documentOperationMapper.insert(documentOperation);
    }

    @Override
    public void restoreDeletedDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        document.setIsDeleted(0);
        documentMapper.updateById(document);
    }

    public void renameDocumentNameLog(Long documentId, String newName,Long userId)  {
        String name =  userDubboService.getUserNameByUserId(userId);
        // 记录用户操作到文档操作表
        DocumentOperation documentOperation = new DocumentOperation();

        documentOperation.setDocumentId(documentId);

        documentOperation.setUserId(userId);

        documentOperation.setOperation("重命名");

        documentOperation.setDescription("用户 " + name + "重新命名为:"+newName);
        documentOperation.setOperationTime(new Date());
        documentOperationMapper.insert(documentOperation);
    }

    private String serializeDocumentInfoVOList(List<DocumentInfoVO> documentInfoVOList) throws Exception {
        return objectMapper.writeValueAsString(documentInfoVOList);
    }

    private List<DocumentInfoVO> deserializeDocumentInfoVOList(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<List<DocumentInfoVO>>() {});
    }

    public void removeDocumentFromCache(Long userId, Long documentId) throws Exception {
        // 定义缓存键
        String cacheKey = "recent_docs:" + userId;
        String cacheInfoVOKey = "recent_docsInfoVO:" + userId;

        System.out.println("redisTemplate = " + redisTemplate);

        // 检查 recent_docs 缓存中是否存在指定的 documentId
        List<Object> documentIdsObj = redisTemplate.opsForList().range(cacheKey, 0, -1);

        // 增加日志记录和 null 检查
        if (documentIdsObj == null) {
            return;
        }

        if (documentIdsObj.isEmpty()) {
            return;
        }

        // 将 Object 类型的列表转换为 String 类型的列表
        List<String> documentIdsStr = documentIdsObj.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        if (!documentIdsStr.contains(String.valueOf(documentId))) {
            return;
        }

        // 从 recent_docs 缓存中删除指定的 documentId
        redisTemplate.opsForList().remove(cacheKey, 1, String.valueOf(documentId));

        // 从 recent_docsInfoVO 缓存中删除对应的 DocumentInfoVO
        String cachedDocumentInfoVOsJson = (String) redisTemplate.opsForValue().get(cacheInfoVOKey);

        if (cachedDocumentInfoVOsJson != null) {
            // 反序列化 JSON 字符串为 List<DocumentInfoVO>
            List<DocumentInfoVO> cachedDocumentInfoVOs = deserializeDocumentInfoVOList(cachedDocumentInfoVOsJson);

            // 过滤掉被删除的 DocumentInfoVO
            List<DocumentInfoVO> updatedDocumentInfoVOs = cachedDocumentInfoVOs.stream()
                    .filter(doc -> !doc.getId().equals(documentId))
                    .collect(Collectors.toList());

            // 更新缓存，如果删除后列表为空，可以选择删除整个缓存
            if (updatedDocumentInfoVOs.isEmpty()) {
                redisTemplate.delete(cacheInfoVOKey);
            } else {
                // 序列化 List<DocumentInfoVO> 为 JSON 字符串
                String updatedDocumentInfoVOsJson = serializeDocumentInfoVOList(updatedDocumentInfoVOs);
                redisTemplate.opsForValue().set(cacheInfoVOKey, updatedDocumentInfoVOsJson, 7, TimeUnit.HOURS);
            }
        } else {
        }
    }
}




