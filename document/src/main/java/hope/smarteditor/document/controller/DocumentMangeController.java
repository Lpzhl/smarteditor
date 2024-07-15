package hope.smarteditor.document.controller;


import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.dto.DocumentPermissionsDTO;
import hope.smarteditor.common.model.dto.TemplateDocumentUpdateDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.TemplateDocument;
import hope.smarteditor.common.model.vo.DocumentShareVO;
import hope.smarteditor.common.model.vo.DocumentUserPermisssVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.annotation.PermissionCheck;
import hope.smarteditor.document.mapper.TemplateDocumentMapper;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.service.DocumentpermissionsService;
import hope.smarteditor.document.service.RecentDocumentsService;
import hope.smarteditor.document.service.TemplateDocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    /**
     * 将在线富文本编写的文档信息上传到数据库中保存
     *  文档上传数据传输对象
     * @return 保存结果
     */
    @ApiOperation("创建文档")
    @PostMapping("/create")
    @LzhLog
    public Result<Document> upload(@RequestBody DocumentUploadDTO documentUploadDTO){
        return Result.success(documentService.saveDocument(documentUploadDTO),ErrorCode.SUCCESS.getCode(), MessageConstant.CREATE_SUCCESSFUL);
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
    public Result renameDocument(@PathVariable("documentId") Long documentId, @RequestParam("newName") String newName) {
        documentService.renameDocument(documentId, newName);
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
    // todo 有问题获取不到
    public Result<List<Document>> getDeletedDocuments(@PathVariable("userId") Long userId) {
        List<Document> deletedDocuments = documentService.getDeletedDocuments(userId);
        return Result.success(deletedDocuments);
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
        List<DocumentShareVO> documentShares = documentService.getDocumentShare(userId);
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
         Long documentId = templateDocumentService.useTemplate(Id, Long.valueOf(request.getHeader("userId")));
         return Result.success(documentId);
     }

}
