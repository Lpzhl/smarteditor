package hope.smarteditor.document.controller;


import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.service.DocumentService;
import hope.smarteditor.document.service.FolderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("folder")
@Slf4j
@Api(tags = "文件夹管理接口")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private DocumentService documentService;

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
    @PostMapping("/deleteFolder/{folderId}")
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
    @PostMapping("/deleteFolder/{documentId}")
    @LzhLog
    @ApiOperation("在文件夹中删除文档")
    public Result deleteDocument(@PathVariable Long documentId){
        return Result.success(folderService.deleteDocument(documentId), ErrorCode.SUCCESS.getCode(), MessageConstant.DELETE_SUCCESSFUL);
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

}
