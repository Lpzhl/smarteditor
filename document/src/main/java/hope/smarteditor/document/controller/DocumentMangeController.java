package hope.smarteditor.document.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.common.model.vo.*;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.annotation.PermissionCheck;
import hope.smarteditor.document.mapper.FolderMapper;
import hope.smarteditor.document.mapper.TemplateDocumentMapper;
import hope.smarteditor.document.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static hope.smarteditor.common.constant.MessageConstant.DEF;

/**
 * (document)表控制层
 *
 * @author lzh
 * @since 2024-05-22 13:51:13
 */
@RestController
@RequestMapping("document")
@Slf4j
@Api(tags = "文档管理接口")
public class DocumentMangeController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentpermissionsService documentpermissionsService;

    @Autowired
    private RecentDocumentsService recentDocumentsService;

    @Autowired
    private TemplateDocumentService templateDocumentService;

    @Autowired
    private DocumentOperationService documentOperationService;

    @Autowired
    private DocumentVersionService documentVersionService;

    /**
     * 将在线富文本编写的文档信息上传到数据库中保存
     *  文档上传数据传输对象
     * @return 保存结果
     */
    @ApiOperation("创建文档")
    @PostMapping("/create")
    @LzhLog
    public Result<Document> upload(@RequestBody DocumentUploadDTO documentUploadDTO){
        Document document = documentService.saveDocument(documentUploadDTO);
        documentService.createLog(document.getId(), document.getUserId());
        return Result.success(document,ErrorCode.SUCCESS.getCode(), MessageConstant.CREATE_SUCCESSFUL);
    }

    /**
     * 更新文档信息
     *
     * @param documentId 文档ID
     * @param documentUpdateDTO 文档更新数据传输对象
     * @return 更新结果
     */

    @ApiOperation("更新文档信息")
    @PutMapping("/update/{documentId}")
    @LzhLog
    @PermissionCheck(value = {"可管理", "可编辑"})
    public Result<Document> updateDocument(@PathVariable("documentId") Long documentId, @RequestBody DocumentUpdateDTO documentUpdateDTO) {
        try {
            Document document = documentService.updateDocument(documentId, documentUpdateDTO);
            documentService.saveLog(documentId, documentUpdateDTO);
            return Result.success(document);
        } catch (Exception e) {
            log.error(ErrorCode.UPDATE_FILE_ERROR.getMessage(), e);
            return Result.error(ErrorCode.UPDATE_FILE_ERROR.getMessage());
        }
    }

    /**
     * 文档重命名
     */
    @ApiOperation("文档重命名")
    @PutMapping("/rename/{documentId}")
    @LzhLog
    @PermissionCheck(value = {"可管理", "可编辑"})
    public Result renameDocument(@PathVariable("documentId") Long documentId, @RequestParam("newName") String newName,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        documentService.renameDocument(documentId, newName, userId);
        return Result.success(MessageConstant.SUCCESSFUL);
    }


    /**
     * 删除文档
     * @param documentId 文档ID
     * @return 删除结果
     */
    @PermissionCheck(value = {"可管理", "可编辑"})
    @ApiOperation("删除文档")
    @DeleteMapping("/delete/{documentId}")
    @LzhLog
    public Result deleteDocument(@PathVariable("documentId") Long documentId) {
        try {
            boolean isDeleted = documentService.deleteDocument(documentId);
            String message = isDeleted ? MessageConstant.DELETE_SUCCESSFUL : MessageConstant.DELETE_FAILED;
            if(isDeleted){
                return Result.success(message);
            }else {
                return Result.error(message);
            }
        } catch (Exception e) {
            log.error( MessageConstant.DELETE_SUCCESSFUL, e);
            return Result.error(MessageConstant.DELETE_FAILED);
        }
    }

    /**
     * 设置文档的对外权限
     */
    @PermissionCheck(value = {"可管理"})
    @PutMapping("/setVisibility/{documentId}")
    @LzhLog
    @ApiOperation("设置文档对外权限")
    public  Result<Document> setDocumentVisibility(@PathVariable("documentId")Long documentId) {
        documentService.setDocumentVisibility(documentId);
        return Result.success(MessageConstant.SET_SUCCESSFUL);
    }

    /**
     * 设置文档对指定用户的权限
     */
    @PermissionCheck(value = {"可管理"})
    @PostMapping("setUserAbility")
    @LzhLog
    @ApiOperation("设置文档对指定用户的权限")
    public Result setUserAbility(@RequestBody DocumentPermissionsDTO documentpermissionsDTO) {
        documentpermissionsService.setUserAbility(documentpermissionsDTO);
        return Result.success(MessageConstant.SET_SUCCESSFUL);
    }

    /**
     * 获取用户已经删除的文档
     */
    @GetMapping("/getDeletedDocuments/{userId}")
    @LzhLog
    @ApiOperation("获取用户已经删除的文档")
    public Result getDeletedDocuments(@PathVariable("userId") Long userId) {
        List<DocumentInfoVO> deletedDocuments = documentService.getDeletedDocuments(userId);
        return Result.success(deletedDocuments);
    }

    /**
     * 将已经删除的文档复原
     */
    @PutMapping("/restoreDeletedDocument/{documentId}")
    @LzhLog
    @ApiOperation("将已经删除的文档复原")
    public Result restoreDeletedDocument(@PathVariable("documentId") Long documentId) {
        documentService.restoreDeletedDocument(documentId);
        return Result.success(MessageConstant.SUCCESSFUL);
    }

    /**
     * 获取一个文档的所有参与者
     */
    @GetMapping("/getParticipants/{documentId}")
    @LzhLog
    @ApiOperation("获取一个文档的所有参与者")
    public Result<List<DocumentUserPermisssVO>> getParticipants(@PathVariable("documentId") Long documentId) {
        List<DocumentUserPermisssVO> participants = documentService.getParticipants(documentId);
        return Result.success(participants);
    }

    /**
     * 根据id查看文档
     */
    @ApiOperation("根据id查看文档")
    @GetMapping("/getDocumentById/{documentId}")
    @LzhLog
    public Result<Document> getDocumentById(@PathVariable("documentId") Long documentId,HttpServletRequest request)  {
        Long userId = Long.valueOf(request.getHeader("userId"));
        Document document = documentService.getDocumentById(userId,documentId);
        recentDocumentsService.recordDocumentAccess(userId, documentId);
        return Result.success(document);
    }

    /**
     * 将文档设置为模板
     */
    @PostMapping("/setDocumentAsTemplate/{documentId}")
    @ApiOperation("将文档设置为模板")
    @LzhLog
    public Result setDocumentAsTemplate(@PathVariable("documentId")Long documentId,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        documentService.setDocumentAsTemplate(documentId, userId);
        return Result.success(MessageConstant.SET_SUCCESSFUL);
    }

    /**
     * 编辑模板
     */
    @PutMapping("/editTemplate")
    @ApiOperation("编辑模板")
    @LzhLog
    public Result editTemplate(@RequestBody TemplateDocumentUpdateDTO templateDocument)  {
        documentService.editTemplate(templateDocument);
        return Result.success(MessageConstant.SUCCESSFUL);
    }


    /**
     * 首页模板展示
     */
    @ApiOperation("首页模板展示")
    @LzhLog
    @GetMapping("/getTemplateShow")
    public Result getTemplateShow() {
        List<TemplateDocument> templateDocuments = documentService.getTemplateShow();
        return Result.success(templateDocuments);
    }

    /**
     * 获取分享
     */
    @ApiOperation("获取分享")
    @LzhLog
    @GetMapping("/getShare/{userId}")
    public Result getShare(@PathVariable("userId") Long userId)  {
        List<DocumentShareInVO> documentShares = documentService.getDocumentShare(userId);
        return Result.success(documentShares);
    }



    /**
     * 批量删除
     */
    @PostMapping("/deleteDocumentBatch")
    @LzhLog
    @ApiOperation("批量删除文档")
    public Result deleteDocumentBatch(@RequestBody List<Long> documentIds){
        documentService.deleteDocumentBatch(documentIds);
        return Result.success(MessageConstant.DELETE_SUCCESSFUL);
    }

    /**
     * 获取模板文档
     */
    @ApiOperation("获取我的模板文档")
    @LzhLog
    @GetMapping("/getTemplateDocument/{userId}")
    public Result getTemplateDocument(@PathVariable("userId") Long userId)  {
        List<TemplateDocument> documentShares = documentService.getTemplateDocument(userId);
        return Result.success(documentShares);
    }


    /**
     * 将模板保存为自己的模板
     */
     @ApiOperation("将模板保存为自己的模板")
     @LzhLog
      @PostMapping("/saveTemplate/{Id}")
     public Result saveTemplate(@PathVariable("Id")Long Id,HttpServletRequest request) {
        templateDocumentService.saveTemplate(Id, Long.valueOf(request.getHeader("userId")));
        return Result.success(MessageConstant.SUCCESSFUL);
     }


    /**
     * 使用模板
     */
     @ApiOperation("使用模板")
     @LzhLog
     @PostMapping("/useTemplate/{Id}")
     public Result useTemplate(@PathVariable("Id")Long Id,HttpServletRequest request) {
         // 先判断该用户是否为会员用户 会员用户免费使用 非会员用户需要查看用户模板使用的次数
         Long documentId = templateDocumentService.useTemplate(Id, Long.valueOf(request.getHeader("userId")));


         return Result.success(documentId);
     }

    /**
     * 获取一个文档的所有操作日志
     */
     @ApiOperation("获取一个文档的所有操作日志")
     @LzhLog
     @GetMapping("/getDocumentLog/{documentId}")
     public Result getDocumentLog(@PathVariable("documentId")Long documentId) {
         List<DocumentOperation> documentLogs = documentOperationService.getDocumentLog(documentId);
         return Result.success(documentLogs);
     }

    /**
     * 获取模板
     */
     @ApiOperation("获取所有模板")
     @LzhLog
     @GetMapping("/getTemplateDocument")
     public Result getTemplate()  {
         QueryWrapper<TemplateDocument> templateDocumentQueryWrapper = new QueryWrapper<>();
         return Result.success(templateDocumentService.list(templateDocumentQueryWrapper));
     }

    /**
     * 获取文档版本记录
     */
     @ApiOperation("获取文档版本记录")
     @LzhLog
     @GetMapping("/getDocumentVersion/{documentId}")
     public Result getDocumentVersion(@PathVariable("documentId")Long documentId)  {
         List<DocumentVersionVO> documentVersions = documentVersionService.getDocumentVersion(documentId);
         return Result.success(documentVersions);
     }

    /**
     * 回退版本
     */
     @ApiOperation("回退版本")
     @LzhLog
     @PostMapping("/rollbackDocumentVersion/{documentId}/{Id}")
     public Result rollbackDocumentVersion(@PathVariable("documentId")Long documentId,@PathVariable("Id")Long versionId,HttpServletRequest request) {
         documentVersionService.rollbackDocumentVersion(documentId, versionId, Long.valueOf(request.getHeader("userId")));
         return Result.success(MessageConstant.SUCCESSFUL);
     }

}
