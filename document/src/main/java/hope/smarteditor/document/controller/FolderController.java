package hope.smarteditor.document.controller;


import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.DocumentFolder;
import hope.smarteditor.common.model.vo.FolderOperationLogVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.mapper.DocumentFolderMapper;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.service.FolderOperationLogService;
import hope.smarteditor.document.service.FolderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("folder")
@Slf4j
@Api(tags = "文件夹管理接口")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentFolderMapper documentFolderMapper;

    @Autowired
    private FolderOperationLogService folderOperationLogService;

    /**
     * 用户创建文件夹
     */
    @PostMapping("/createFolder")
    @LzhLog
    @ApiOperation("用户创建文件夹")
    public Result createFolder(@RequestBody FolderDTO folderDTO){
        return Result.success(folderService.createFolder(folderDTO),ErrorCode.SUCCESS.getCode(),MessageConstant.CREATE_SUCCESSFUL);
    }

    /**
     * 用户删除文件夹
     * @param folderId
     * @return
     */
    @PostMapping("/deleteFolders/{folderId}")
    @LzhLog
    @ApiOperation("用户删除文件夹")
    public Result deleteFolder(@PathVariable Long folderId){
       boolean b = folderService.deleteFolder(folderId);
        if (b) {
            return Result.success(MessageConstant.DELETE_SUCCESSFUL);
        } else {
            return Result.error(MessageConstant.DELETE_FAILED);
        }
    }

    /**
     * 用户修改文件夹（名字）
     */
    @PostMapping("/updateFolder")
    @LzhLog
    @ApiOperation("用户修改文件夹")
    public Result updateFolder(@RequestBody FolderUpdateDTO folderDTO, HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(folderService.updateFolder(folderDTO,userId),ErrorCode.SUCCESS.getCode(),MessageConstant.UPDATE_SUCCESSFUL);
    }

    /**
     * 文件夹的权限设置
     */
    @PostMapping("/setFolderPermission")
    @LzhLog
    @ApiOperation("文件夹的权限设置")
    public Result setFolderPermission(@RequestBody FolderPermissionUpdateDTO folderDTO){
        return Result.success(folderService.setFolderPermission(folderDTO),ErrorCode.SUCCESS.getCode(),MessageConstant.SET_SUCCESSFUL);
    }

    /**
     * 在文件夹中创建文档
     */
    @PostMapping("/createDocument")
    @LzhLog
    @ApiOperation("在文件夹中创建文档")
    public Result createDocument(@RequestBody DocumentFolderDTO documentFolderDTO, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        Document document = documentService.saveDocument(documentFolderDTO.getDocumentUploadDTO());
        return Result.success(folderService.createDocument(documentFolderDTO.getFolderId(), document,userId), ErrorCode.SUCCESS.getCode(), MessageConstant.CREATE_SUCCESSFUL);
    }

    /**
     * 在文件夹中删除文档
     */
    @PostMapping("/deleteDocument/{folderId}/{documentId}")
    @LzhLog
    @ApiOperation("在文件夹中删除文档")
    public Result deleteDocument(@PathVariable Long folderId, @PathVariable Long documentId,HttpServletRequest request) {
        boolean isDeleted = folderService.deleteDocument(documentId, folderId, Long.valueOf(request.getHeader("userId")));
        return Result.success(isDeleted, ErrorCode.SUCCESS.getCode(), MessageConstant.DELETE_SUCCESSFUL);
    }
    /**
     * 在各类文件夹中移动文档
     */
    @PostMapping("/moveDocument")
    @LzhLog
    @ApiOperation("在各类文件夹中移动文档")
    public Result moveDocument(@RequestBody MoveDocumentDTO moveDocumentDTO,HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(folderService.moveDocument(moveDocumentDTO,userId), ErrorCode.SUCCESS.getCode(), MessageConstant.MOVE_SUCCESSFUL);
    }

    /**
     * 将文档移动到文件夹
     */
    @PostMapping("/moveDocumentToFolder")
    @LzhLog
    @ApiOperation("将文档移动到文件夹")
    public Result moveDocumentToFolder(@RequestBody MoveDocumentToFolderDTO moveDocumentToFolderDTO,HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(folderService.moveDocumentToFolder(moveDocumentToFolderDTO,userId), ErrorCode.SUCCESS.getCode(), MessageConstant.MOVE_SUCCESSFUL);
    }

    /**
     * 获取一个用户的所有文件夹文档信息
     */
    @GetMapping("/getFolderDocument")
    @LzhLog
    @ApiOperation("获取一个用户的所有文件夹文档信息")
    public Result getFolderDocument(HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(folderService.getFolderDocument(userId), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 将文档放入默认的文件夹中
     */
     @PostMapping("/moveDocumentToDefault")
     @LzhLog
     @ApiOperation("将文档放入默认的文件夹中")
     public Result moveDocumentToDefault(@RequestBody MoveDocumentToFolderDTO moveDocumentToFolderDTO){
         DocumentFolder documentFolder = new DocumentFolder();
         documentFolder.setDocumentId(moveDocumentToFolderDTO.getDocumentId());
         documentFolder.setFolderId(moveDocumentToFolderDTO.getFolderId());
         return Result.success(documentFolderMapper.insert(documentFolder), ErrorCode.SUCCESS.getCode(), MessageConstant.MOVE_SUCCESSFUL);
     }

    /**
     * 根据文件夹id获取文件夹中所有的文档信息
     */
     @GetMapping("/getDocumentByFolderId")
     @LzhLog
     @ApiOperation("根据文件夹id获取文件夹中所有的文档信息")
     public Result getDocumentByFolderId(@RequestParam Long folderId){
           return Result.success(folderService.getDocumentByFolderId(folderId), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
     }

    /**
     * 删除某个用户的最近文档
     */
     @PostMapping("/deleteRecentDocument/{documentId}")
     @LzhLog
     @ApiOperation("删除某个用户的最近文档")
    public Result deleteRecentDocument(@PathVariable Long documentId,HttpServletRequest request){
        Long userId = Long.valueOf(request.getHeader("userId"));
        return Result.success(folderService.deleteRecentDocument(documentId,userId), ErrorCode.SUCCESS.getCode(), MessageConstant.DELETE_SUCCESSFUL);
    }

    /**
     * 获取某个文件夹的所有操作日志
     */
     @GetMapping("/getFolderLog/{folderId}")
     @LzhLog
     @ApiOperation("获取某个文件夹的所有操作日志")

    public Result<List<FolderOperationLogVO>> getFolderLog(@PathVariable("folderId") Long folderId){
     return Result.success(folderOperationLogService.getFolderLog(folderId), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
     }

    /**
     * 批量删除文件夹中的文档信息
     */
     @PostMapping("/deleteDocumentByFolderId")
     @LzhLog
     @ApiOperation("批量删除文件夹中的文档信息")
     public Result deleteDocumentByFolderId(@RequestBody DeleteDocumentByFolderIdDTO deleteDocumentByFolderIdDTO,HttpServletRequest request){
         return Result.success(folderService.deleteDocumentByFolderId(deleteDocumentByFolderIdDTO, Long.valueOf(request.getHeader("userId"))), ErrorCode.SUCCESS.getCode(), MessageConstant.DELETE_SUCCESSFUL);
     }

    /**
     * 恢复删除的文档
     */
     @PostMapping("/recoverDocument")
     @LzhLog
     @ApiOperation("恢复删除的文档")
     public Result recoverDocument(@RequestBody RecoverDocumentDTO recoverDocumentDTO,HttpServletRequest request){
         return Result.success(folderService.recoverDocument(recoverDocumentDTO, Long.valueOf(request.getHeader("userId"))), ErrorCode.SUCCESS.getCode(), MessageConstant.RECOVER_SUCCESSFUL);
     }

}
