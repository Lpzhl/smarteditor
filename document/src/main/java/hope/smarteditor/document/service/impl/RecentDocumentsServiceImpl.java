package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.FavoriteDocument;
import hope.smarteditor.common.model.entity.RecentDocuments;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.DocumentInfoVO;
import hope.smarteditor.common.model.vo.RecentDocumentsVO;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.mapper.FavoriteDocumentMapper;
import hope.smarteditor.document.mapper.FavoriteTemplateMapper;
import hope.smarteditor.document.service.RecentDocumentsService;
import hope.smarteditor.document.mapper.RecentDocumentsMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author LoveF
 * @description 针对表【recent_documents】的数据库操作Service实现
 * @createDate 2024-07-06 00:49:18
 */
@Service
public class RecentDocumentsServiceImpl extends ServiceImpl<RecentDocumentsMapper, RecentDocuments>
        implements RecentDocumentsService{



    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private RecentDocumentsMapper recentDocumentMapper;

    @Autowired
    private FavoriteDocumentMapper favoriteDocumentMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    private static final String RECENT_DOCUMENTS_KEY_PREFIX = "recent_documents:";
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void recordDocumentAccess(Long userId, Long documentId) {
        RecentDocuments recentDocument = recentDocumentMapper.findByUserIdAndDocumentId(userId, documentId);

        if (recentDocument != null) {
            // 如果记录已存在，则更新访问时间
            recentDocumentMapper.updateById(recentDocument);
        } else {
            // 如果记录不存在，则插入新记录
            recentDocument = new RecentDocuments();
            recentDocument.setUserId(userId);
            recentDocument.setDocumentId(documentId);
            recentDocumentMapper.insert(recentDocument);
        }
    }



/*    public void recordDocumentAccess(Long userId, Long documentId) {
        String key = RECENT_DOCUMENTS_KEY_PREFIX + userId;

        long currentTime = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(key, String.valueOf(documentId), currentTime);

        redisTemplate.opsForZSet().removeRange(key, 0, -51);

        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }*/


/*
    @Override
    public List<RecentDocumentsVO> getRecentDocuments(Long userId) {
        String key = RECENT_DOCUMENTS_KEY_PREFIX + userId;
        Set<String> recentDocumentIds = redisTemplate.opsForZSet().reverseRange(key, 0, 49);
        List<Long> documentIds;
        if (recentDocumentIds == null || recentDocumentIds.isEmpty()) {
            documentIds = recentDocumentMapper.selectRecentDocumentIdsByUserId(userId, 50);
        } else {
        documentIds = recentDocumentIds.stream()
                    .map(id -> id.replaceAll("[^\\d]", ""))
                    .filter(id -> !id.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        List<RecentDocumentsVO> result = new ArrayList<>();
        if (!documentIds.isEmpty()) {
            QueryWrapper<RecentDocuments> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("document_id", documentIds).eq("user_id", userId);
            List<RecentDocuments> recentDocs = recentDocumentMapper.selectList(queryWrapper);

            // 使用 Map 来按照时间类型分类文档
            Map<String, List<DocumentInfoVO>> categoryMap = new HashMap<>();

            for (RecentDocuments recentDoc : recentDocs) {
                DocumentInfoVO documentInfoVO = new DocumentInfoVO();
                Document document = documentMapper.selectById(recentDoc.getDocumentId());

                if (document != null) {
                    BeanUtils.copyProperties(document, documentInfoVO);
                    // 设置文档创建用户信息和收藏状态
                    User userInfo = userDubboService.getUserInfoByUserId(document.getUserId());
                    documentInfoVO.setCreateUserNickname(userInfo.getNickname());
                    boolean isFavorited = checkIfDocumentIsFavorited(String.valueOf(userId), document.getId());
                    documentInfoVO.setIsFavorite(isFavorited);

                    // 设置时间类型
                    String category = getTimeCategory(recentDoc.getAccessTime());

                    // 将文档信息添加到对应时间类型的列表中
                    if (!categoryMap.containsKey(category)) {
                        categoryMap.put(category, new ArrayList<>());
                    }
                    categoryMap.get(category).add(documentInfoVO);
                }
            }

            // 构造 RecentDocumentsVO 对象列表
            for (Map.Entry<String, List<DocumentInfoVO>> entry : categoryMap.entrySet()) {
                RecentDocumentsVO vo = new RecentDocumentsVO();
                vo.setDocumentInfoVOList(entry.getValue()); // 设置文档信息列表
                vo.setCategory(entry.getKey()); // 设置时间类型
                result.add(vo);
            }
        }

        return result;
    }
*/


    @Override
    public List<DocumentInfoVO> getRecentDocuments(Long userId) {
        List<Long> documentIds = recentDocumentMapper.selectRecentDocumentIdsByUserId(userId, 50);

        List<DocumentInfoVO> result = new ArrayList<>();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));

        for (Long documentId : documentIds) {
            Document document = documentMapper.selectById(documentId);
            QueryWrapper<RecentDocuments> recentDocumentsQueryWrapper = new QueryWrapper<>();
            recentDocumentsQueryWrapper.eq("document_id", documentId).eq("user_id", userId);
             RecentDocuments recentDocuments = recentDocumentMapper.selectOne(recentDocumentsQueryWrapper);
            if (document != null) {
                DocumentInfoVO documentInfoVO = new DocumentInfoVO();
                BeanUtils.copyProperties(document, documentInfoVO);

                User userInfo = userDubboService.getUserInfoByUserId(document.getUserId());
                documentInfoVO.setCreateUserNickname(userInfo.getNickname());
                boolean isFavorited = checkIfDocumentIsFavorited(String.valueOf(userId), document.getId());
                documentInfoVO.setIsFavorite(isFavorited);
                documentInfoVO.setUpdateTime(recentDocuments.getAccessTime());
                result.add(documentInfoVO);
            }
        }

        return result;
    }


    private boolean checkIfDocumentIsFavorited(String userId, Long documentId) {

        QueryWrapper<FavoriteDocument> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", userId).eq("document_id", documentId);

        int count = favoriteDocumentMapper.selectCount(queryWrapper);
        return count > 0;
    }

    private String getTimeCategory(Date accessTime) {
        LocalDate accessDate = accessTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate sevenDaysAgo = today.minusDays(7);

        if (accessDate.equals(today)) {
            return "今天";
        } else if (accessDate.equals(yesterday)) {
            return "昨天";
        } else if (accessDate.isAfter(sevenDaysAgo)) {
            return "7天内";
        } else {
            return "更早";
        }
    }



/*    @Scheduled(fixedRate = 360000)
    public void syncRecentDocumentsToDatabase() {
        Set<String> keys = redisTemplate.keys(RECENT_DOCUMENTS_KEY_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                Long userId = Long.valueOf(key.split(":")[1]);
                Set<String> recentDocumentIds = redisTemplate.opsForZSet().range(key, 0, -1);
                if (recentDocumentIds != null) {
                    for (String documentId : recentDocumentIds) {
                            Long docId = Long.valueOf(documentId);
                        RecentDocuments recentDocument = recentDocumentMapper.findByUserIdAndDocumentId(userId, docId);
                        if (recentDocument != null) {
                            recentDocument.setAccessTime(new Timestamp(System.currentTimeMillis()));
                        } else {
                            recentDocument = new RecentDocuments();

                            recentDocument.setUserId(userId);

                            recentDocument.setDocumentId(docId);
                            recentDocumentMapper.insert(recentDocument);
                        }

                    }
                }
            }
        }
    }*/




}




