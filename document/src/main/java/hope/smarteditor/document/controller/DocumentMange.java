package hope.smarteditor.document.controller;


import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.dto.DocumentPermissionsDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.DocumentOperation;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.annotation.PermissionCheck;
import hope.smarteditor.document.mapper.DocumentOperationMapper;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.service.DocumentpermissionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
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
public class DocumentMange {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentpermissionsService documentpermissionsService;




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
    public Result<Document> updateDocument(@PathVariable Long documentId, @RequestBody DocumentUpdateDTO documentUpdateDTO) {
        try {
            Document document = documentService.updateDocument(documentId, documentUpdateDTO);
            return Result.success(document);
        } catch (Exception e) {
            log.error(ErrorCode.UPDATE_FILE_ERROR.getMessage(), e);
            return Result.error(ErrorCode.UPDATE_FILE_ERROR.getMessage());
        }
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
    public Result deleteDocument(@PathVariable Long documentId) {
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
    public  Result<Document> setDocumentVisibility(@PathVariable Long documentId) {
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
    public Result<List<Document>> getDeletedDocuments(@PathVariable Long userId) {
        List<Document> deletedDocuments = documentService.getDeletedDocuments(userId);
        return Result.success(deletedDocuments);
    }
}
