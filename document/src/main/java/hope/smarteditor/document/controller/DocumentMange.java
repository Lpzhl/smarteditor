package hope.smarteditor.document.controller;


import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.service.DocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 将在线富文本编写的文档信息上传到数据库中保存
     *
     *  文档上传数据传输对象
     * @return 保存结果
     */
    @ApiOperation("文档信息保存")
    @PostMapping("/upload")
    @LzhLog
    public Result<Document> upload(@RequestBody DocumentUploadDTO documentUploadDTO) throws Exception{
        return Result.success(documentService.saveDocument(documentUploadDTO));
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
    public Result<Document> updateDocument(@PathVariable Long documentId, @RequestBody DocumentUpdateDTO documentUpdateDTO) {
        try {
            Document document = documentService.updateDocument(documentId, documentUpdateDTO);
            return Result.success(document);
        } catch (Exception e) {
            log.error("更新文档失败", e);
            return Result.error("更新文档失败");
        }
    }

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @return 删除结果
     */
    @ApiOperation("删除文档")
    @DeleteMapping("/delete/{documentId}")
    @LzhLog
    public Result deleteDocument(@PathVariable Long documentId) {
        try {
            boolean isDeleted = documentService.deleteDocument(documentId);
            String message = isDeleted ? "删除成功" : "删除失败";
            if(isDeleted){
                return Result.success(message);
            }else {
                return Result.error(message);
            }
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return Result.error("删除文档失败");
        }
    }


}
