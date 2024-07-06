package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.RecentDocuments;
import hope.smarteditor.common.model.vo.RecentDocumentsVO;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.service.RecentDocumentsService;
import hope.smarteditor.document.mapper.RecentDocumentsMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private RecentDocumentsMapper recentDocumentMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    private static final String RECENT_DOCUMENTS_KEY_PREFIX = "recent_documents:";

    public void recordDocumentAccess(Long userId, Long documentId) {
        String key = RECENT_DOCUMENTS_KEY_PREFIX + userId;
        String value = String.valueOf(documentId);
        redisTemplate.opsForZSet().add(key, value, System.currentTimeMillis());
        redisTemplate.opsForZSet().removeRange(key, 0, -51); // 保留最近访问的 50 个文档
    }

    public List<RecentDocumentsVO> getRecentDocuments(Long userId) {
        String key = RECENT_DOCUMENTS_KEY_PREFIX + userId;
        Set<String> recentDocumentIds = redisTemplate.opsForZSet().reverseRange(key, 0, 49);
        List<Long> documentIds;

        if (recentDocumentIds == null || recentDocumentIds.isEmpty()) {
            documentIds = recentDocumentMapper.selectRecentDocumentIdsByUserId(userId, 50);
        } else {
            documentIds = recentDocumentIds.stream().map(Long::valueOf).collect(Collectors.toList());
        }

        List<RecentDocumentsVO> result = new ArrayList<>();
        if (!documentIds.isEmpty()) {
            QueryWrapper<RecentDocuments> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("document_id", documentIds).eq("user_id", userId);
            List<RecentDocuments> recentDocs = recentDocumentMapper.selectList(queryWrapper);

            for (RecentDocuments recentDoc : recentDocs) {
                Document document = documentMapper.selectById(recentDoc.getDocumentId());
                if (document != null) {
                    RecentDocumentsVO vo = new RecentDocumentsVO();
                    vo.setDocument(document);
                    vo.setUser(userDubboService.getUserInfoByUserId(document.getUserId()));
                    vo.setCategory(getTimeCategory(recentDoc.getAccessTime()));
                    result.add(vo);
                }
            }
        }

        return result;
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




    @Scheduled(fixedRate = 360000) // 每小时同步一次
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

                        }
                        recentDocumentMapper.insert(recentDocument);
                    }
                }
            }
        }
    }


}




