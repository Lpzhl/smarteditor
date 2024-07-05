package hope.smarteditor.document.controller;

import hope.smarteditor.common.model.dto.ShareDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.RecentDocuments;
import hope.smarteditor.common.model.entity.Share;
import hope.smarteditor.common.model.vo.RecentDocumentsVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.mapper.RecentDocumentsMapper;
import hope.smarteditor.document.service.RecentDocumentsService;
import hope.smarteditor.document.service.ShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("share")
@Slf4j
@Api(tags = "分享相关")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @Autowired
    private RecentDocumentsService recentDocumentsService;

    /**
     * 分享文档
     */
    @PostMapping("/document")
    @ApiOperation("分享文档")
    @LzhLog
    public Result shareDocument(@RequestBody ShareDTO shareDTO) {
        log.info("分享文档");
        Long id = shareDTO.getId();
        Integer validDays = shareDTO.getValidDays();
        String editPermission = shareDTO.getEditPermission();

        // 1.生成唯一的链接并保存到数据库
        Share documentShare = shareService.shareDocument(id, validDays, editPermission);

        // 2.返回链接
        return Result.success(documentShare.getLink());
    }


    /**
     * 分享文件夹
     */
    @PostMapping("/folder")
    @ApiOperation("分享文件夹")
    @LzhLog
    public Result shareFolder(@RequestBody ShareDTO shareDTO) {
        log.info("分享文件夹");
        Long id = shareDTO.getId();
        Integer validDays = shareDTO.getValidDays();
        String editPermission = shareDTO.getEditPermission();

        // 1.生成唯一的链接并保存到数据库
        Share documentShare = shareService.shareFolder(id, validDays, editPermission);

        // 2.返回链接
        return Result.success(documentShare.getLink());
    }

    /**
     * 处理分享的文档
     */

    @GetMapping("/document/{link}")
    @ApiOperation("处理分享的文档")
    @LzhLog
    public Result handleShareDocument(@PathVariable String link) {
        return Result.success(shareService.handleShareDocument(link));
    }


    @PostMapping("/access")
    @ApiOperation("记录文档访问")
    @LzhLog
    public void recordDocumentAccess(@RequestParam Long userId, @RequestParam Long documentId) {
        recentDocumentsService.recordDocumentAccess(userId, documentId);
    }

    @GetMapping("/recent")
    @ApiOperation("获取最近访问的文档")
    @LzhLog
    public Result<List<RecentDocumentsVO>> getRecentDocuments(@RequestParam Long userId) {
        return Result.success(recentDocumentsService.getRecentDocuments(userId));
    }
}