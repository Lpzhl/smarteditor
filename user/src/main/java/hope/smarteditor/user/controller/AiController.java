package hope.smarteditor.user.controller;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.vo.BaiduResultVO;
import hope.smarteditor.common.model.vo.OcrVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.UserService;
import io.minio.errors.*;
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
import java.util.List;

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
    @LzhLog
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
    @LzhLog
    public Result textCorrection(@RequestParam("text") String text) {
        /*System.out.println("text = " + text);*/
        String correctedText = userService.textCorrection(text);
        //String correctedText = "文本纠错";
        if (correctedText == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(correctedText, ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_CORRECTION_SUCCESSFUL);
    }

    /**
     * 标题生成
     */
    @PostMapping("/titleGeneration")
    @ApiOperation("标题生成")
    @LzhLog
    public Result titleGeneration(@RequestParam("text") String text) {
        String s = userService.titleGeneration(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.TITLE_GENERATION_SUCCESSFUL);
    }

    /**
     * 文本摘要
     */
    @PostMapping("/textSummarization")
    @ApiOperation("文本摘要")
    @LzhLog
    public Result textSummarization(@RequestParam("text") String text) {
        String s = userService.textSummarization(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_SUMMARIZATION_SUCCESSFUL);
    }

    /**
     * 续写
     */
    @PostMapping("/textContinuation")
    @ApiOperation("续写")
    @LzhLog
    public Result textContinuation(@RequestParam("text") String text,@RequestParam("passage") String passage) {
        String s = userService.textContinuation(text, passage);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_CONTINUATION_SUCCESSFUL);
    }

    /**
     * 论文内容生成
     */
    @PostMapping("/paperContentGeneration")
    @ApiOperation("论文内容生成")
    @LzhLog
    public Result paperContentGeneration(@RequestParam("text") String text,@RequestParam("project") String project,@RequestParam("paper_type") String paper_type,@RequestParam("directory_type") String  directory_type) {
        String s = userService.paperContentGeneration(text, project, paper_type, directory_type);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 论文大纲生成
     */
    @PostMapping("/paperOutlineGeneration")
    @ApiOperation("论文大纲生成")
    @LzhLog
    public Result paperOutlineGeneration(@RequestParam("text") String text,@RequestParam("project") String project,@RequestParam("paper_type") String paper_type,@RequestParam("directory_type") String  directory_type) {
        String s = userService.paperOutlineGeneration(text, project, paper_type, directory_type);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 百度
     */

    @PostMapping("/baidu")
    @ApiOperation("百度")
    @LzhLog
    public Result baidu(@RequestParam("text") String text) {
        System.out.println("百度text = " + text);
        List<BaiduResultVO> s = userService.baidu(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }


    /**
     * 表格识别
     */
     @PostMapping("/ocrTable")
     @ApiOperation("表格识别")
     @LzhLog
     public Result ocrTable(@RequestParam("file") MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
         return Result.success(userService.ocrTable(file),ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
     }

    /**
     * 音频识别ASR
     */
     @PostMapping("/asr")
     @ApiOperation("音频识别ASR")
     @LzhLog
     public Result asr(@RequestParam("file") MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
            return Result.success(userService.asr(file),ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
     }

    /**
     * 图标生成
     */
     @PostMapping("/createChart")
     @ApiOperation("图标生成")
     @LzhLog
     public Result createChart(@RequestParam("text") String text) {
         String s = userService.createChart(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 格式
     */
     @PostMapping("/fixFormat")
     @ApiOperation("格式")
     @LzhLog
     public Result format(@RequestParam("text") String text) {
     String s = userService.format(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     *重写
     */
     @PostMapping("/rewrite")
     @ApiOperation("重写")
     @LzhLog
     public Result rewrite(@RequestParam("text") String text) {
         String s = userService.rewrite(text);
         //String s = "重写";
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
         return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 扩写
     */
     @PostMapping("/expansion")
     @ApiOperation("扩写")
     @LzhLog
      public Result expansion(@RequestParam("text") String text) {
          String s = userService.expansion(text);
          //String s = "扩写";
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);

     }

    /**
     * 缩写
     */
     @PostMapping("/abbreviation")
     @ApiOperation("缩写")
     @LzhLog
     public Result abbreviation(@RequestParam("text") String text) {
         String s = userService.abbreviation(text);
         //String s = "缩写";
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);

     }

    /**
     * 文本润色
     */
     @PostMapping("/textBeautification")
     @ApiOperation("文本润色")
     @LzhLog
     public Result polish(@RequestParam("text") String text, @RequestParam("requirement") String requirement)  {
         String s = userService.polish(text,requirement);
         //String s = "润色";
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 数据可视化
     */
     @PostMapping("/dataVisualization")
     @ApiOperation("数据可视化")
     @LzhLog
     public Result dataVisualization(@RequestParam("text") String text,@RequestParam("image_type") String image_type)   {
         String s = userService.dataVisualization(text,image_type);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 翻译
     */
     @PostMapping("/translate")
     @ApiOperation("翻译")
     @LzhLog
     public Result translate(@RequestParam("text") String text)   {
         String s = userService.translate(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 思维导图
     */
     @PostMapping("/mindMap")

 @ApiOperation("思维导图")

 @LzhLog

 public Result mindMap(@RequestParam("text") String text)   {
         String s = userService.mindMap(text);
        if (s == null) {
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);

     }
}
