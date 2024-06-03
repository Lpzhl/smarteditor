package hope.smarteditor.document.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.exception.GlobalExceptionHandler;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Documentpermissions;
import hope.smarteditor.document.mapper.DocumentpermissionsMapper;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.mapper.DocumentMapper;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        Document document =documentMapper.selectById(documentId);
        if (document == null) {
            return null;
        }

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
        if (documentUpdateDTO.getName() != null) {
            document.setName(documentUpdateDTO.getName());
        }
        if (documentUpdateDTO.getType() != null) {
            document.setType(documentUpdateDTO.getType());
        }
        if (documentUpdateDTO.getLabel() != null) {
            document.setLabel(documentUpdateDTO.getLabel());
        }

        document.setUpdateTime(new Date());
        documentMapper.updateById(document);

        // 更新Redis中缓存的信息
        String cacheKey = "document:" + documentId;
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        if (hasKey != null && hasKey) {
            Document updatedDocument = documentMapper.selectById(documentId);
            redisTemplate.opsForValue().set(cacheKey, updatedDocument);
            redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForValue().set(cacheKey, document);
        }


        return document;
    }


    @Override
    public boolean deleteDocument(Long documentId) {
        int i = documentMapper.deleteById(documentId);
        // 删除缓存的信息
        if (i > 0) {
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




