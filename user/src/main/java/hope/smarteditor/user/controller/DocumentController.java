package hope.smarteditor.user.controller;


import hope.smarteditor.api.DocumentDubboService;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.FavoriteDocumentDTO;
import hope.smarteditor.common.model.dto.FavoriteTemplateDTO;
import hope.smarteditor.common.model.dto.UserDocumentLikeDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.vo.DocumentInfoVO;
import hope.smarteditor.common.model.vo.FavoriteDocumentVO;
import hope.smarteditor.common.model.vo.FavoriteTemplateVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/documents")
@Api(tags = "文档相关接口")
public class DocumentController {

    @DubboReference(version = "1.0.0", group = "document", check = false)
    private DocumentDubboService documentDubboService;

    /**
     * 获取用户所有文档信息
     * @param userId
     * @return
     */
    @GetMapping("/getAllUserDocument/{userId}")
    @LzhLog
    @ApiOperation("获取用户所有文档信息")
    public Result<List<DocumentInfoVO>> getAllUserDocument(@PathVariable("userId") Long userId) {
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
    public Result<Document> getDocumentById(@PathVariable("documentId") Long documentId) {
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
        return isFavorited ? Result.success(MessageConstant.ALREADY_FAVORITED) : Result.success(MessageConstant.FAVORITE_CANCELED);
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
        return Result.success(isFavorited ? MessageConstant.ALREADY_FAVORITED :MessageConstant.FAVORITE_CANCELED);
    }


    /**
     *  获取用户的所有收藏文档
     * @param userId
     * @return
     */
    @GetMapping("/userFavoriteDocuments/{userId}")
    @LzhLog
    @ApiOperation("获取用户的所有收藏文档")
    public Result getUserFavoriteDocuments(@PathVariable("userId") Long userId) {
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
    public Result getUserFavoriteTemplates(@PathVariable("userId") Long userId) {
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
        return b ? Result.success(MessageConstant.ALREADY_LIKED):Result.success(MessageConstant.FAVORITE_CANCELED);
    }






}
