package hope.smarteditor.document.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.exception.GlobalExceptionHandler;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.dto.TemplateDocumentUpdateDTO;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.common.model.vo.*;
import hope.smarteditor.document.mapper.*;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.service.FolderService;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
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

        documentUploadDTO.setContent("默认文本");

        Document document = new Document();
        document.setUserId(documentUploadDTO.getUserId());
        if(documentUploadDTO.getName() == null){
            document.setName("未命名文档");
        }else{
            document.setName(documentUploadDTO.getName());
        }
        document.setContent(documentUploadDTO.getContent());
        document.setSummary(documentUploadDTO.getSummary());
        document.setType(documentUploadDTO.getType());
        document.setLabel(documentUploadDTO.getLabel());
        document.setStatus(documentUploadDTO.getStatus());
        document.setCategory(documentUploadDTO.getCategory());
        document.setSubject(documentUploadDTO.getSubject());
        document.setCreateTime(new Date());
        document.setUpdateTime(new Date());

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

        // 保存旧版本到文档版本表
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentId(document.getId());
        documentVersion.setVersion(getNextVersionNumber(document.getId()));
        documentVersion.setContent(document.getContent());
        documentVersion.setSummary(document.getSummary());
        documentVersion.setUsername(userDubboService.getUserNameByUserId(document.getUserId()));
        documentVersion.setUpdateTime(new Date());
        documentVersionMapper.insert(documentVersion);

        // 更新cacheKey1
        String cacheKey1 = "user:" + document.getUserId() + ":documents";
        List<Document> userDocuments = (List<Document>) redisTemplate.opsForValue().get(cacheKey1);
        if (userDocuments != null) {
            userDocuments.add(savedDocument);
            redisTemplate.opsForValue().set(cacheKey1, userDocuments);
            redisTemplate.expire(cacheKey1, 7, TimeUnit.DAYS);
        }

        // 保存到Redis中，以文档的ID作为key
        String cacheKey = "document:" + document.getId();
        redisTemplate.opsForValue().set(cacheKey, savedDocument);
        // 设置缓存过期时间，例如1小时
        redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);

        // 搜索该用户的默认文件夹并且 添加到默认文件夹中
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
        redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);

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
    public boolean deleteDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return false;
        }

        // 执行逻辑删除  并且设置删除时间
        int i = documentMapper.deleteById(documentId);

        // 删除最近文档的记录
        LambdaQueryWrapper<RecentDocuments> recentDocumentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recentDocumentsLambdaQueryWrapper.eq(RecentDocuments::getDocumentId, documentId);
        recentDocumentsMapper.delete(recentDocumentsLambdaQueryWrapper);

        if (i > 0) {
            // 删除cacheKey1中的文档信息
            String cacheKey1 = "user:" + document.getUserId() + ":documents";
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
        List<Document> deletedDocuments = documentMapper.getDeletedDocuments(userId);
        List<DocumentInfoVO> documentInfoVOS = deletedDocuments.stream()
                .map(document -> convertToDocumentInfoVO(document, userId))
                .collect(Collectors.toList());
        return documentInfoVOS;
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
        documentQueryWrapper.eq("id", documentId);
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
        // 获取用户的所有最近文档
        List<RecentDocuments> recentDocuments = recentDocumentsMapper.selectList(new QueryWrapper<RecentDocuments>().eq("user_id", userId));

        if (recentDocuments.isEmpty()) {
            return Collections.emptyList(); // 如果没有最近文档，则直接返回空列表
        }

        // 获取最近文档的documentId列表
        List<Long> documentIds = recentDocuments.stream()
                .map(RecentDocuments::getDocumentId)
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

        // 将两个对象添加到列表中并返回
        List<DocumentShareInVO> documentShareInVOList = new ArrayList<>();
        documentShareInVOList.add(sentDocumentShareInVO);
        documentShareInVOList.add(receivedDocumentShareInVO);

        return documentShareInVOList;
    }

    private DocumentInfoVO convertToDocumentInfoVO(Document document, Long userId) {
        DocumentInfoVO documentInfoVO = new DocumentInfoVO();
        BeanUtils.copyProperties(document, documentInfoVO);
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



    private  boolean checkIfDocumentIsFavorited(String userId, Long documentId) {

        QueryWrapper<FavoriteDocument> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", userId).eq("document_id", documentId);

        int count = favoriteDocumentMapper.selectCount(queryWrapper);
        return count > 0;
    }
    @Override
    public SearchVO searchDocumentsByContent(String keyword, Long userId) {
        SearchVO searchVO = new SearchVO();
        QueryWrapper<Document> documentQueryWrapper = new QueryWrapper<>();
        documentQueryWrapper.like("content", keyword);
        List<Document> documents = documentMapper.selectList(documentQueryWrapper);
        searchVO.setDocuments(documents);
        return searchVO;
    }

    @Override
    public void deleteDocumentBatch(List<Long> documentIds) {

        // 删除文档
        documentMapper.deleteBatchIds(documentIds);
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

}




