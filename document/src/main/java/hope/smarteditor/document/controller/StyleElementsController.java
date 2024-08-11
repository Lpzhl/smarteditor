package hope.smarteditor.document.controller;


import hope.smarteditor.common.model.dto.StyleDeleteDTO;
import hope.smarteditor.common.model.dto.StyleEditDTO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.LzhLog;
import hope.smarteditor.document.service.FontSettingsService;
import hope.smarteditor.document.service.StylesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("style")
@Slf4j
@Api(tags = "样式相关")
public class StyleElementsController {

    @Resource
    private StylesService stylesService;

    @Resource
    private FontSettingsService fontSettingsService;


    /**
     * 根据用户Id获取用户的样式
     */
    @ApiOperation(value = "根据用户Id获取用户的样式")
    @LzhLog
    @GetMapping("getStyleByUserId/{userId}")
    public Result getStyleByUserId(@PathVariable("userId") Long userId) {
        return Result.success(stylesService.getStyleByUserId(userId));
    }

    /**
     * 编辑样式
     * @param styleEditDTO
     * @return
     */
    @ApiOperation(value = "编辑样式")
    @LzhLog
    @PostMapping("editStyle")
    @Transactional
    public Result editStyle(@RequestBody StyleEditDTO styleEditDTO) {
        return Result.success(stylesService.editStyle(styleEditDTO));
    }

    /**
     * 删除样式
     * @param styleDeleteDTO
     * @return
     */
    @ApiOperation(value = "删除样式")
    @LzhLog
    @PostMapping("deleteStyle")
    public Result<String> deleteStyle(@RequestBody StyleDeleteDTO styleDeleteDTO) {
        return Result.success(stylesService.deleteStyle(styleDeleteDTO));
    }

    /**
     * 新增
     */
    @ApiOperation(value = "新增样式")
    @LzhLog
    @PostMapping("addStyle")
    public Result addStyle(@RequestBody StyleEditDTO styleEditDTO) {
        return Result.success(stylesService.addStyle(styleEditDTO));
    }

}

