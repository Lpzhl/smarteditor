package hope.smarteditor.document.dubboImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hope.smarteditor.api.DocumentDubboService;
import hope.smarteditor.common.model.dto.FavoriteDocumentDTO;
import hope.smarteditor.common.model.dto.FavoriteTemplateDTO;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.common.model.vo.FavoriteDocumentVO;
import hope.smarteditor.common.model.vo.FavoriteTemplateVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.mapper.*;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * author lzh
 */

@Service
@DubboService
public class DocumentDubboServiceImpl implements DocumentDubboService {

    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private FavoriteDocumentMapper favoriteDocumentMapper;
    @Resource
    private FavoriteTemplateMapper favoriteTemplateMapper;
    @Resource
    private TemplateDocumentMapper templateDocumentMapper;
    @Resource
    private UserDocumentLikeMapper userDocumentLikeMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Document> getUserAllDocumentInfo(String userId) {
        String cacheKey = "user:" + userId + ":documents";

        // 尝试从Redis缓存中获取文档信息
        List<Document> documents = (List<Document>) redisTemplate.opsForValue().get(cacheKey);

        if (documents == null) {
            // 如果缓存中不存在文档信息，则从数据库中获取
            QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            documents = documentMapper.selectList(queryWrapper);

            if (documents != null && !documents.isEmpty()) {
                // 将获取到的文档信息存储到Redis缓存中，并设置过期时间
                redisTemplate.opsForValue().set(cacheKey, documents);
                redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);
            }
        }

        return documents;
    }

    @Override
    public Document getDocumentById(Long documentId) {
        String cacheKey = "document:" + documentId;

        // 尝试从Redis缓存中获取文档
        Document document = (Document) redisTemplate.opsForValue().get(cacheKey);

        if (document != null) {
            // 如果缓存中存在该文档信息，则直接返回
            return document;
        } else {
            // 如果缓存中不存在该文档信息，则从数据库中获取
            QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", documentId);
            document = documentMapper.selectOne(queryWrapper);

            if (document != null) {
                // 将获取到的文档信息存储到Redis缓存中，并设置过期时间
                redisTemplate.opsForValue().set(cacheKey, document);
                redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);
            }

            return document;
        }
    }


    @Override
    public List<FavoriteDocumentVO> getUserFavoriteDocuments(Long userId) {
        QueryWrapper<FavoriteDocument> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        List<FavoriteDocument> favoriteDocuments = favoriteDocumentMapper.selectList(queryWrapper);
        List<FavoriteDocumentVO> favoriteDocumentVOList = new ArrayList<>();

        for (FavoriteDocument favoriteDocument : favoriteDocuments) {
            FavoriteDocumentVO favoriteDocumentVO = new FavoriteDocumentVO();
            BeanUtils.copyProperties(favoriteDocument, favoriteDocumentVO);

            // 先尝试从Redis缓存中获取文档信息
            String cacheKey = "document:" + favoriteDocument.getDocumentId();
            Document document = (Document) redisTemplate.opsForValue().get(cacheKey);

            if (document == null) {
                // 如果缓存中不存在该文档信息，则从数据库中获取
                document = documentMapper.selectById(favoriteDocument.getDocumentId());
                if (document != null) {
                    // 将获取到的文档信息存储到Redis缓存中，并设置过期时间
                    redisTemplate.opsForValue().set(cacheKey, document);
                    redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS);
                }
            }

            favoriteDocumentVO.setDocument(document);
            favoriteDocumentVOList.add(favoriteDocumentVO);
        }

        return favoriteDocumentVOList;
    }


    @Override
    public boolean toggleFavoriteDocument(FavoriteDocumentDTO favoriteDocumentDTO) {
        QueryWrapper<FavoriteDocument> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", favoriteDocumentDTO.getUserId())
                .eq("document_id", favoriteDocumentDTO.getDocumentId());

        FavoriteDocument favoriteDocument = favoriteDocumentMapper.selectOne(queryWrapper);
        if (favoriteDocument == null) {
            // 收藏文档
            favoriteDocument = new FavoriteDocument();
            BeanUtils.copyProperties(favoriteDocumentDTO,favoriteDocument);
            System.out.println("favoriteDocumentDTO = " + favoriteDocumentDTO);
            System.out.println("favoriteDocument = " + favoriteDocument);
            favoriteDocumentMapper.insert(favoriteDocument);
            return true; // 表示已收藏
        } else {
            // 取消收藏
            favoriteDocumentMapper.delete(queryWrapper);
            return false; // 表示已取消收藏
        }
    }

    @Override
    public boolean toggleFavoriteTemplate(FavoriteTemplateDTO favoriteTemplateDTO) {
        QueryWrapper<FavoriteTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", favoriteTemplateDTO.getUserId())
                .eq("template_id", favoriteTemplateDTO.getTemplateId());

        FavoriteTemplate favoriteTemplate = favoriteTemplateMapper.selectOne(queryWrapper);
        if (favoriteTemplate == null) {
            // 收藏模板
            favoriteTemplate = new FavoriteTemplate();
            BeanUtils.copyProperties(favoriteTemplateDTO, favoriteTemplate);
            favoriteTemplateMapper.insert(favoriteTemplate);
            return true; // 表示已收藏
        } else {
            // 取消收藏
            favoriteTemplateMapper.delete(queryWrapper);
            return false; // 表示已取消收藏
        }
    }

    @Override
    public List<FavoriteTemplateVO> getUserFavoriteTemplates(Long userId) {
        QueryWrapper<FavoriteTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        List<FavoriteTemplate> favoriteTemplates = favoriteTemplateMapper.selectList(queryWrapper);
        List<FavoriteTemplateVO> favoriteTemplateVOList = new ArrayList<>();

        for (FavoriteTemplate favoriteTemplate : favoriteTemplates) {
            FavoriteTemplateVO favoriteTemplateVO = new FavoriteTemplateVO();
            BeanUtils.copyProperties(favoriteTemplate, favoriteTemplateVO);

            TemplateDocument templateDocument = templateDocumentMapper.selectById(favoriteTemplate.getTemplateId());
            favoriteTemplateVO.setDocument(templateDocument);

            favoriteTemplateVOList.add(favoriteTemplateVO);
        }

        return favoriteTemplateVOList;
    }

    @Override
    public boolean likeDocument(Long documentId, Long userId) {
        //1. 查询用户是否已经点赞过该文档
        QueryWrapper<UserDocumentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("document_id", documentId)
                .eq("user_id", userId);
        UserDocumentLike userDocumentLike = userDocumentLikeMapper.selectOne(queryWrapper);

        // 1.1根据document_id查找到文档信息便于后期修改
        QueryWrapper<Document> documentQueryWrapper = new QueryWrapper<>();
        documentQueryWrapper.eq("id", documentId);
        Document document = documentMapper.selectOne(documentQueryWrapper);

        //2. 如果没有点赞过，则点赞文档，并增加点赞数
        if (userDocumentLike==null){
            UserDocumentLike newUserDocumentLike = new UserDocumentLike();
            newUserDocumentLike.setDocumentId(documentId);
            newUserDocumentLike.setUserId(userId);
            userDocumentLikeMapper.insert(newUserDocumentLike);
            // 更新文档的点赞数
            document.setId(documentId);
            document.setLikeCount(document.getLikeCount()+1);
            documentMapper.updateById(document);
            return true;
        }
        //3. 如果已经点赞过，则取消点赞并且删除点赞记录表的记录，并减少点赞数，
        userDocumentLikeMapper.deleteById(userDocumentLike.getId());

        // 更新文档的点赞数
        document.setId(documentId);
        document.setLikeCount(document.getLikeCount() - 1);
        documentMapper.updateById(document);

        return false;
    }

}
