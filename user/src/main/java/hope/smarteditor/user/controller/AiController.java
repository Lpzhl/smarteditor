package hope.smarteditor.user.controller;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.vo.OcrVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.service.UserService;
import io.minio.errors.MinioException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/ai")
@Api(tags = "AI关接口")
public class AiController {


    @Autowired
    private UserService userService;
    /**
     * ocr
     */
    @PostMapping("/ocr")
    @ApiOperation("OCR识别")
    public Result<OcrVO> ocr(@RequestParam("file") MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, MinioException, IOException {
        OcrVO ocrvo = userService.ocr(file);
        if (ocrvo != null) {
            return Result.success(ocrvo, ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
        } else {
            return Result.error(MessageConstant.FILE_UPLOAD_FAILED,500);
        }
    }


    /**
     * 文本纠错
     */
    @PostMapping("/textCorrection")
    @ApiOperation("文本纠错")
    public Result textCorrection(@RequestParam("text") String text) {
        return Result.success(userService.textCorrection(text), ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_CORRECTION_SUCCESSFUL);
    }

    /**
     * 标题生成
     */
    @PostMapping("/titleGeneration")
    @ApiOperation("标题生成")
    public Result titleGeneration(@RequestParam("text") String text) {
        return Result.success(userService.titleGeneration(text), ErrorCode.SUCCESS.getCode(), MessageConstant.TITLE_GENERATION_SUCCESSFUL);
    }

    /**
     * 文本摘要
     */
    @PostMapping("/textSummarization")
    @ApiOperation("文本摘要")
    public Result textSummarization(@RequestParam("text") String text) {
        return Result.success(userService.textSummarization(text), ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_SUMMARIZATION_SUCCESSFUL);
    }

    /**
     * 续写
     */
    @PostMapping("/textContinuation")
    @ApiOperation("续写")
    public Result textContinuation(@RequestParam("text") String text,@RequestParam("passage") String passage) {
        return Result.success(userService.textContinuation(text,passage), ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_CONTINUATION_SUCCESSFUL);
    }

    /**
     * 论文内容生成
     */
    @PostMapping("/paperContentGeneration")
    @ApiOperation("论文内容生成")
    public Result paperContentGeneration(@RequestParam("text") String text,@RequestParam("project") String project,@RequestParam("paper_type") String paper_type,@RequestParam("directory_type") String  directory_type) {
        return Result.success(userService.paperContentGeneration(text,project,paper_type,directory_type), ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 论文大纲生成
     */
    @PostMapping("/paperOutlineGeneration")
    @ApiOperation("论文大纲生成")
    public Result paperOutlineGeneration(@RequestParam("text") String text,@RequestParam("project") String project,@RequestParam("paper_type") String paper_type,@RequestParam("directory_type") String  directory_type) {
        return Result.success(userService.paperOutlineGeneration(text,project,paper_type,directory_type), ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 百度
     */

    @PostMapping("/baidu")
    @ApiOperation("百度")
    public Result baidu(@RequestParam("text") String text) {
        return Result.success(userService.baidu(text), ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 翻译
     */


}
