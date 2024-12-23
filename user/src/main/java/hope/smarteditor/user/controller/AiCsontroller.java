package hope.smarteditor.user.controller;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.PaperReviewRequestDTO;
import hope.smarteditor.common.model.entity.ApiCalls;
import hope.smarteditor.common.model.entity.ApiInfo;
import hope.smarteditor.common.model.vo.BaiduResultVO;
import hope.smarteditor.common.model.vo.OcrVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.ApiCallsService;
import hope.smarteditor.user.service.ApiInfoService;
import hope.smarteditor.user.service.UserService;
import io.minio.errors.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/testai")
@Api(tags = "AI相关接口")
public class AiCsontroller {

    @Autowired
    private UserService userService;

    @Resource
    private ApiCallsService apiCallsService;

    @Resource
    private ApiInfoService apiInfoService;
    /**
     * ocr
     */
    @PostMapping("/ocr")
    @ApiOperation("OCR识别")
    @LzhLog
    public Result<OcrVO> ocr(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws NoSuchAlgorithmException, InvalidKeyException, MinioException, IOException {
        Long userId = Long.valueOf(request.getHeader("userId"));
        OcrVO ocrvo = userService.ocr(file);
        if (ocrvo != null) {
            createApiCalls("OCR识别",userId,ErrorCode.SUCCESS.getCode());
            return Result.success(ocrvo, ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
        } else {
            createApiCalls("OCR识别",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.FILE_UPLOAD_FAILED,500);
        }
    }


    /**
     * 文本纠错
     */
    @PostMapping("/textCorrection")
    @ApiOperation("文本纠错")
    @LzhLog
    public Result textCorrection(@RequestParam("text") String text,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        /*System.out.println("text = " + text);*/
        String correctedText = userService.textCorrection(text);
        //String correctedText = "文本纠错";
        if (correctedText == null) {
            createApiCalls("文本纠错",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("文本纠错",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(correctedText, ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_CORRECTION_SUCCESSFUL);
    }

    /**
     * 标题生成
     */
    @PostMapping("/titleGeneration")
    @ApiOperation("标题生成")
    @LzhLog
    public Result titleGeneration(@RequestParam("text") String text,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        String s = userService.titleGeneration(text);
        if (s == null) {
            createApiCalls("标题生成",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("标题生成",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.TITLE_GENERATION_SUCCESSFUL);
    }

    /**
     * 文本摘要
     */
    @PostMapping("/textSummarization")
    @ApiOperation("文本摘要")
    @LzhLog
    public Result textSummarization(@RequestParam("text") String text,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        String s = userService.textSummarization(text);
        if (s == null) {
            createApiCalls("文本摘要",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("文本摘要",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_SUMMARIZATION_SUCCESSFUL);
    }

    /**
     * 续写
     */
    @PostMapping("/textContinuation")
    @ApiOperation("续写")
    @LzhLog
    public Result textContinuation(@RequestParam("text") String text,@RequestParam("passage") String passage,HttpServletRequest request)  {
        Long userId = Long.valueOf(request.getHeader("userId"));
        String s = userService.textContinuation(text, passage);
        if (s == null) {
            createApiCalls("续写",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("续写",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.TEXT_CONTINUATION_SUCCESSFUL);
    }

    /**
     * 论文内容生成
     */
    @PostMapping("/paperContentGeneration")
    @ApiOperation("论文内容生成")
    @LzhLog
    public Result paperContentGeneration(@RequestParam("text") String text,@RequestParam("project") String project,@RequestParam("paper_type") String paper_type,@RequestParam("directory_type") String  directory_type,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        String s = userService.paperContentGeneration(text, project, paper_type, directory_type);
        if (s == null) {
            createApiCalls("论文内容生成",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("论文内容生成",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 论文大纲生成
     */
    @PostMapping("/paperOutlineGeneration")
    @ApiOperation("论文大纲生成")
    @LzhLog
    public Result paperOutlineGeneration(@RequestParam("text") String text,@RequestParam("project") String project,@RequestParam("paper_type") String paper_type,@RequestParam("directory_type") String  directory_type,HttpServletRequest request)  {
        String s = userService.paperOutlineGeneration(text, project, paper_type, directory_type);
        Long userId = Long.valueOf(request.getHeader("userId"));
        if (s == null) {
            createApiCalls("论文大纲生成",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("论文大纲生成",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }

    /**
     * 百度
     */

    @PostMapping("/baidu")
    @ApiOperation("百度")
    @LzhLog
    public Result baidu(@RequestParam("text") String text,HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("userId"));
        List<BaiduResultVO> s = userService.baidu(text);
        if (s == null) {
            createApiCalls("百度",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("百度",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }


    /**
     * 表格识别
     */
     @PostMapping("/ocrTable")
     @ApiOperation("表格识别")
     @LzhLog
     public Result ocrTable(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Long userId = Long.valueOf(request.getHeader("userId"));
         OcrVO ocrVO = userService.ocrTable(file);
         if (ocrVO == null) {
             createApiCalls("表格识别",userId,ErrorCode.NETWORK_ERROR.getCode());
             return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
         }
         createApiCalls("表格识别",userId,ErrorCode.SUCCESS.getCode());
         return Result.success(ocrVO,ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
     }

    /**
     * 音频识别ASR
     */
     @PostMapping("/asr")
     @ApiOperation("音频识别ASR")
     @LzhLog
     public Result asr(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
         Long userId = Long.valueOf(request.getHeader("userId"));
         OcrVO asr = userService.asr(file);
         if (asr == null) {
             createApiCalls("音频识别ASR",userId,ErrorCode.NETWORK_ERROR.getCode());
             return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
         }
         createApiCalls("音频识别ASR",userId,ErrorCode.SUCCESS.getCode());
         return Result.success(asr,ErrorCode.SUCCESS.getCode(), MessageConstant.FILE_UPLOAD_SUCCESSFUL);
     }

    /**
     * 图标生成
     */
     @PostMapping("/createChart")
     @ApiOperation("图标生成")
     @LzhLog
     public Result createChart(@RequestParam("text") String text,HttpServletRequest request) {
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.createChart(text);
        if (s == null) {
            createApiCalls("图标生成",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("图标生成",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 格式
     */
     @PostMapping("/fixFormat")
     @ApiOperation("格式")
     @LzhLog
     public Result format(@RequestParam("text") String text,HttpServletRequest request)  {
         Long userId = Long.valueOf(request.getHeader("userId"));
     String s = userService.format(text);
        if (s == null) {
            createApiCalls("格式",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("格式",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     *重写
     */
     @PostMapping("/rewrite")
     @ApiOperation("重写")
     @LzhLog
     public Result rewrite(@RequestParam("text") String text,HttpServletRequest request) {
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.rewrite(text);
         //String s = "重写";
        if (s == null) {
            createApiCalls("重写",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("重写",userId,ErrorCode.SUCCESS.getCode());
         return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 扩写
     */
     @PostMapping("/expansion")
     @ApiOperation("扩写")
     @LzhLog
      public Result expansion(@RequestParam("text") String text,HttpServletRequest request)  {
         Long userId = Long.valueOf(request.getHeader("userId"));
          String s = userService.expansion(text);
          //String s = "扩写";
        if (s == null) {
            createApiCalls("扩写",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("扩写",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);

     }

    /**
     * 缩写
     */
     @PostMapping("/abbreviation")
     @ApiOperation("缩写")
     @LzhLog
     public Result abbreviation(@RequestParam("text") String text,HttpServletRequest request) {
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.abbreviation(text);
         //String s = "缩写";
        if (s == null) {
            createApiCalls("缩写",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("缩写",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);

     }

    /**
     * 文本润色
     */
     @PostMapping("/textBeautification")
     @ApiOperation("文本润色")
     @LzhLog
     public Result polish(@RequestParam("text") String text, @RequestParam("requirement") String requirement,HttpServletRequest request){
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.polish(text,requirement);
         //String s = "润色";
        if (s == null) {
            createApiCalls("文本润色",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("文本润色",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 数据可视化
     */
     @PostMapping("/dataVisualization")
     @ApiOperation("数据可视化")
     @LzhLog
     public Result dataVisualization(@RequestParam("text") String text,@RequestParam("image_type") String image_type,HttpServletRequest request)   {
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.dataVisualization(text,image_type);
        if (s == null) {
            createApiCalls("数据可视化",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("数据可视化",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 翻译
     */
     @PostMapping("/translate")
     @ApiOperation("翻译")
     @LzhLog
     public Result translate(@RequestParam("text") String text,HttpServletRequest request)   {
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.translate(text);
        if (s == null) {
            createApiCalls("翻译",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("翻译",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 思维导图
     */
     @PostMapping("/mindMap")
     @ApiOperation("思维导图")
     @LzhLog
     public Result mindMap(@RequestParam("text") String text,HttpServletRequest request)   {
         String s = userService.mindMap(text);
         Long userId  = Long.valueOf(request.getHeader("userId"));
        if (s == null) {
            createApiCalls("思维导图",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("思维导图",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);

     }

    /**
     * 论文评审
     */
    @PostMapping("/paperReview")
    @ApiOperation("论文评审")
    @LzhLog
    public Result paperReview(@RequestBody PaperReviewRequestDTO request,HttpServletRequest requests) {
        String text = request.getText();
        Long userId = Long.valueOf(requests.getHeader("userId"));
        String s = userService.paperReview(text);
        if (s == null) {
            createApiCalls("论文评审",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR, ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("论文评审",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
    }


    /**
     * AI文档助手
     */
     @PostMapping("/aiDocumentAssistant")
     @ApiOperation("AI文档助手")
     @LzhLog
     public Result aiDocumentAssistant(@RequestParam("problem") String text, @RequestParam("document") String documentUrl, HttpServletRequest request){
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.aiDocumentAssistant(text,documentUrl);
        if (s == null) {
            createApiCalls("AI文档助手",userId,ErrorCode.NETWORK_ERROR.getCode());
            return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
        }
        createApiCalls("AI文档助手",userId,ErrorCode.SUCCESS.getCode());
        return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }


     @ApiOperation("获取热点新闻")
     @LzhLog
     @PostMapping("/hot")
     public Result test()   {
         return Result.success(userService.fetchToutiaoHot());
     }



    /**
     * 可视化
     */
     @PostMapping("/visualize")
     @ApiOperation("可视化")
     @LzhLog
     public Result visualize(@RequestParam("text") String text, @RequestParam("image_type") String imageType, HttpServletRequest request){
         Long userId = Long.valueOf(request.getHeader("userId"));
         String s = userService.visualize(text,imageType);
         if (s == null) {
             createApiCalls("数据可视化",userId,ErrorCode.NETWORK_ERROR.getCode());
             return Result.error(MessageConstant.NETWORK_ERROR,ErrorCode.NETWORK_ERROR.getCode());
         }
         createApiCalls("数据可视化",userId,ErrorCode.SUCCESS.getCode());
         return Result.success(s, ErrorCode.SUCCESS.getCode(), MessageConstant.SUCCESSFUL);
     }

    /**
     * 扣费
     */
     @PostMapping("/deduct")
     @ApiOperation("扣费")
     @LzhLog
     public Result deduct(HttpServletRequest request, @RequestParam("apiName") String apiName){
         Long userId = Long.valueOf(request.getHeader("userId"));
         createApiCalls(apiName,userId,ErrorCode.SUCCESS.getCode());
         return Result.success();
     }


    private boolean createApiCalls(String aiName,Long userId,Integer statusCode){
        // 根据接口名字找到接口信息
        ApiInfo apiInfo = apiInfoService.findByName(aiName);
        if (apiInfo == null) {
            return false;
        }
        // 保存调用记录
        ApiCalls apiCall = new ApiCalls();
        apiCall.setApiId(apiInfo.getId());
        apiCall.setUserId(userId);
        apiCall.setStatusCode(statusCode);
        apiCallsService.save(apiCall);
        return true;
    }
}
