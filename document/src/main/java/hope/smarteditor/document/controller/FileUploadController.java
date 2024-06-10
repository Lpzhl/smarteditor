package hope.smarteditor.document.controller;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.service.DocumentService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@Api(tags = "文件上传接口")
public class FileUploadController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    @LzhLog
    public Result uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String fileUrl = documentService.uploadFile(file);
        if (fileUrl != null) {
            return Result.success(fileUrl, ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
        } else {
            return Result.error(MessageConstant.FILE_UPLOAD_FAILED,500);
        }
    }
}
