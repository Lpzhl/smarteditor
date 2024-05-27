package hope.smarteditor.document.controller;

import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    @LzhLog
    public Result uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String fileUrl = documentService.uploadFile(file);
        if (fileUrl != null) {
            return Result.success(fileUrl,200,"文件上传成功");
        } else {
            return Result.error("文件上传失败",500);
        }
    }
}
