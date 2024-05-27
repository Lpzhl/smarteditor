package hope.smarteditor.user.controller;


import hope.smarteditor.api.DocumentDubboService;
import hope.smarteditor.common.model.dto.FavoriteDocumentDTO;
import hope.smarteditor.common.model.dto.FavoriteTemplateDTO;
import hope.smarteditor.common.model.dto.UserDocumentLikeDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.vo.FavoriteDocumentVO;
import hope.smarteditor.common.model.vo.FavoriteTemplateVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/document")
@Api(tags = "文档相关接口")
public class DocumentController {

    @DubboReference
    private DocumentDubboService documentDubboService;


    /**
     * 获取用户所有文档信息
     * @param userId
     * @return
     */
    @GetMapping("/getAllUserDocument/{userId}")
    @LzhLog
    @ApiOperation("获取用户所有文档信息")
    public Result<List<Document>> getAllUserDocument(@PathVariable Long userId) {
        return Result.success(documentDubboService.getUserAllDocumentInfo(String.valueOf(userId)));
    }


    /**
     * 获取文档信息
     * @param documentId
     * @return
     */
    @GetMapping("/getDocumentById/{documentId}")
    @LzhLog
    @ApiOperation("获取文档信息")
    public Result<Document> getDocumentById(@PathVariable Long documentId) {
        return Result.success(documentDubboService.getDocumentById(documentId));
    }


    /**
     * 用户收藏或取消收藏文档或
     */
    @PostMapping("/favoriteDocument")
    @LzhLog
    @ApiOperation("收藏或取消收藏文档")
    public Result toggleFavoriteDocument(@RequestBody FavoriteDocumentDTO favoriteDocumentDTO) {
        boolean isFavorited = documentDubboService.toggleFavoriteDocument(favoriteDocumentDTO);
        return isFavorited ? Result.success("已收藏") : Result.success("已取消收藏");
    }

    /**
     * 用户收藏或取消收藏模板
     * @param favoriteTemplateDTO
     * @return
     */
    @PostMapping("/toggleFavoriteTemplate")
    @LzhLog
    @ApiOperation("收藏或取消收藏模板")
    public Result toggleFavoriteTemplate(@RequestBody FavoriteTemplateDTO favoriteTemplateDTO) {
        boolean isFavorited = documentDubboService.toggleFavoriteTemplate(favoriteTemplateDTO);
        return Result.success(isFavorited ? "已收藏" : "已取消收藏");
    }


    /**
     *  获取用户的所有收藏文档
     * @param userId
     * @return
     */
    @GetMapping("/userFavoriteDocuments/{userId}")
    @LzhLog
    @ApiOperation("获取用户的所有收藏文档")
    public Result getUserFavoriteDocuments(@PathVariable Long userId) {
        List<FavoriteDocumentVO> favoriteDocuments = documentDubboService.getUserFavoriteDocuments(userId);
        return Result.success(favoriteDocuments);
    }

    /**
     * 获取用户的所有收藏模板
     * @param userId
     * @return
     */
    @GetMapping("/userFavoriteTemplates/{userId}")
    @LzhLog
    @ApiOperation("获取用户的所有收藏模板")
    public Result getUserFavoriteTemplates(@PathVariable Long userId) {
        List<FavoriteTemplateVO> favoriteTemplates = documentDubboService.getUserFavoriteTemplates(userId);
        return Result.success(favoriteTemplates);
    }


    /**
     * 点赞或取消点赞文档
     * @param userDocumentLikeDTO
     * @return
     */
    @PostMapping("/likeDocument")
    @LzhLog
    @ApiOperation("点赞或取消点赞文档")
    public Result likeDocument(@RequestBody UserDocumentLikeDTO userDocumentLikeDTO){
        boolean b = documentDubboService.likeDocument(userDocumentLikeDTO.getDocumentId(), userDocumentLikeDTO.getUserId());
        return b ? Result.success("已点赞"):Result.success("已取消点赞");
    }




}
