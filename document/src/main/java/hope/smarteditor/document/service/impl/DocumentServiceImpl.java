package hope.smarteditor.document.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.exception.GlobalExceptionHandler;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.DocumentOperation;
import hope.smarteditor.common.model.entity.DocumentVersion;
import hope.smarteditor.common.model.entity.Documentpermissions;
import hope.smarteditor.document.mapper.DocumentOperationMapper;
import hope.smarteditor.document.mapper.DocumentVersionMapper;
import hope.smarteditor.document.mapper.DocumentpermissionsMapper;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.mapper.DocumentMapper;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private  DocumentMapper documentMapper;

    @Resource
    private DocumentpermissionsMapper documentpermissionsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Autowired
    private DocumentOperationMapper documentOperationMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

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
        Document document = new Document();
        document.setUserId(documentUploadDTO.getUserId());
        document.setName(documentUploadDTO.getName());
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

        // 保存旧版本到文档版本表
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentId(document.getId());
        documentVersion.setVersion(getNextVersionNumber(documentId));
        documentVersion.setContent(document.getContent());
        documentVersion.setSummary(document.getSummary());
        documentVersion.setUpdateTime(new Date());
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
        String name =  userDubboService.getUserNameByUserId(userId);
        // 记录用户操作到文档操作表
        DocumentOperation documentOperation = new DocumentOperation();
        documentOperation.setDocumentId(documentId);
        documentOperation.setUserId(userId);
        documentOperation.setOperation("编辑");
        documentOperation.setDescription("用户 " + name + " 更新了文档《 " + documentMapper.selectById(documentId).getName()+ "》");
        documentOperation.setOperationTime(new Date());
        documentOperationMapper.insert(documentOperation);

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




}




