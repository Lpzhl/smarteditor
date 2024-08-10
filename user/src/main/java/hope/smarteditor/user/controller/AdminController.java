package hope.smarteditor.user.controller;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.ApiInfoDTO;
import hope.smarteditor.common.model.entity.ApiInfo;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.ApiInfoService;
import hope.smarteditor.user.service.OrdersService;
import hope.smarteditor.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
@Api(tags = "后台相关接口")
public class AdminController {

    @Resource
    private ApiInfoService apiInfoService;

    @Resource
    private OrdersService ordersService;

    @Resource
    private UserService userService;


    @ApiOperation(value = "根据接口Id上线与下线接口")
    @PostMapping("/updateApiInfo")
    @LzhLog
    public Result updateApiInfo(@RequestBody ApiInfoDTO apiInfoDTO) {
        ApiInfo apiInfo = apiInfoService.updateApiInfo(apiInfoDTO);
        return Result.success(apiInfo, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    @ApiOperation(value = "获取积分、会员销售额按时间归类")
    @GetMapping("/getScoreAndSales")
    @LzhLog
    public Result getScoreAndSales() {
        return Result.success(ordersService.getScoreAndSales(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }


    @ApiOperation(value = "用户数量统计每日新增")
    @GetMapping("/getUsersCount")
    @LzhLog
    public Result getUsersCount() {
        return Result.success(userService.getUsersCount(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    @ApiOperation(value = "获取未支付订单")
    @GetMapping("/getUnpaidOrders")
    @LzhLog
    public Result getUnpaidOrders() {
        return Result.success(ordersService.getUnpaidOrders(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    @ApiOperation(value = "用户管理（封禁与解封）")
    @GetMapping("/updateUserStatus")
    @LzhLog
    public Result updateUserStatus(@RequestParam("userId") Long userId) {
        userService.updateUserStatus(userId);
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }
}
