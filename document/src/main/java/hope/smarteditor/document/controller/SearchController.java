package hope.smarteditor.document.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Folder;
import hope.smarteditor.common.model.vo.SearchVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.service.FolderService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("search")
@Slf4j
@Api(value = "搜索接口", tags = "搜索接口")
public class SearchController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    /**
     * 根据名字搜索 文档和文件夹
     */
    @GetMapping("byName")
    public Result<SearchVO> search(@RequestParam("keyword") String keyword, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));

        // Redis key
        String redisKey = "search:" + userId;

        // 先在redis中搜索
        String cachedResultJson = (String) redisTemplate.opsForHash().get(redisKey, keyword);
        if (cachedResultJson != null) {
            try {
                SearchVO cachedResult = objectMapper.readValue(cachedResultJson, SearchVO.class);
                return Result.success(cachedResult);
            } catch (JsonProcessingException e) {
                log.error("从Redis缓存解析JSON时出错", e);
            }
        }

        // 如果没有缓存结果，再在数据库中搜索
        List<Document> documents = documentService.searchDocumentsByName(keyword, userId);
        List<Folder> folders = folderService.searchFoldersByName(keyword, userId);

        // 创建 SearchVO 对象
        SearchVO searchResult = new SearchVO();
        searchResult.setDocuments(documents);
        searchResult.setFolders(folders);

        // 将结果缓存到 Redis
        try {
            String searchResultJson = objectMapper.writeValueAsString(searchResult);
            redisTemplate.opsForHash().put(redisKey, keyword, searchResultJson);
            redisTemplate.expire(redisKey, 10, TimeUnit.MINUTES);  // 设置过期时间
        } catch (JsonProcessingException e) {
            log.error("将SearchVO转换为JSON时出错", e);
        }

        return Result.success(searchResult);
    }

    /**
     * 根据创建者名字搜索 文档和文件夹
     */
    @GetMapping("byCreator")
    public Result<List<Folder> >searchByCreator(@RequestParam("keyword") String keyword, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(documentService.searchDocumentsByCreator(keyword, userId));

    }
}
