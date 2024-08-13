package hope.smarteditor.user.controller;

import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.ApiInfoDTO;
import hope.smarteditor.common.model.dto.NoticeDTO;
import hope.smarteditor.common.model.dto.PriceUpdateDTO;
import hope.smarteditor.common.model.entity.ApiInfo;
import hope.smarteditor.common.model.entity.Pricing;
import hope.smarteditor.common.model.entity.PricingHistory;
import hope.smarteditor.common.model.entity.SystemNotification;
import hope.smarteditor.common.model.vo.TotalSalesVO;
import hope.smarteditor.common.model.vo.UnpaidOrdersVO;
import hope.smarteditor.common.model.vo.UserStatisticsVO;
import hope.smarteditor.common.model.vo.UserVO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.config.RedisService;
import hope.smarteditor.user.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Handler;

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

    @Resource
    private PricingService pricingService;

    @Autowired
    private RedisService redisService;

    @Resource
    private SystemNotificationService systemNotificationService;

    @ApiOperation(value = "根据接口Id上线与下线接口")
    @PostMapping("/updateApiInfo")
    @LzhLog
    public Result<ApiInfo> updateApiInfo(@RequestBody ApiInfoDTO apiInfoDTO) {
        ApiInfo apiInfo = apiInfoService.updateApiInfo(apiInfoDTO);
        return Result.success(apiInfo, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    @ApiOperation(value = "获取积分、会员销售额按时间归类")
    @GetMapping("/getScoreAndSales")
    @LzhLog
    public Result<List<TotalSalesVO>> getScoreAndSales() {
        return Result.success(ordersService.getScoreAndSales(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }


    @ApiOperation(value = "用户数量统计每日新增")
    @GetMapping("/getUsersCount")
    @LzhLog
    public Result<UserStatisticsVO> getUsersCount() {
        return Result.success(userService.getUsersCount(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    @ApiOperation(value = "获取未支付订单")
    @GetMapping("/getUnpaidOrders")
    @LzhLog
    public Result<List<UnpaidOrdersVO>> getUnpaidOrders() {
        return Result.success(ordersService.getUnpaidOrders(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 获取用户列表
     * @param userId
     * @return
     */
    @ApiOperation(value = "获取用户列表")
    @GetMapping("/getUserList")
    @LzhLog
    public Result<List<UserVO>> getUserList() {
        return Result.success(userService.getUserList(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    @ApiOperation(value = "用户管理（封禁与解封）")
    @PostMapping("/updateUserStatus")
    @LzhLog
    public Result updateUserStatus(@RequestParam("userId") Long userId) {
        userService.updateUserStatus(userId);
        redisService.deleteLike(userId.toString());
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 获取价格表
     */
    @ApiOperation(value = "获所有正常取价格表")
    @GetMapping("/getPriceTable")
    @LzhLog
    public Result<List<Pricing>> getPriceTable() {
        return Result.success(pricingService.getPriceTable(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 删除价格表
     */
    @ApiOperation(value = "删除价格表")
    @DeleteMapping("/deletePriceTable")
    @LzhLog
    public Result deletePriceTable(@RequestParam("id") Long id, HttpServletRequest request)  {
        String userId = request.getHeader("userId");
        pricingService.deletePriceTable(id, Long.valueOf(userId));
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 获取已删除的价格表
     */
    @ApiOperation(value = "获取已删除的价格表")
    @GetMapping("/getDeletedPriceTable")
    @LzhLog
    public Result<List<Pricing> > getDeletedPriceTable() {
        return Result.success(pricingService.getDeletedPriceTable(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }


    /**
     * 编辑价格表
     */
    @ApiOperation(value = "管理价格表")
    @PostMapping("/managePriceTable")
    @LzhLog
    public Result managePriceTable(@RequestBody PriceUpdateDTO priceUpdateDTO) {
        pricingService.managePriceTable(priceUpdateDTO);
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 获取价格操作日志
     */
    @ApiOperation(value = "获取价格操作日志")
    @GetMapping("/getPriceTableLog")
    @LzhLog
    public Result<List<PricingHistory>> getPriceTableLog() {
        return Result.success(pricingService.getPriceTableLog(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 发布通知
     */
    @ApiOperation(value = "发布通知")
    @PostMapping("/publishNotice")
    @LzhLog
    public Result<SystemNotification> publishNotice(@RequestBody NoticeDTO noticeDTO) {
        systemNotificationService.publishNotice(noticeDTO);
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 编辑通知
     */
    @ApiOperation(value = "编辑通知")
    @PostMapping("/editNotice")
    @LzhLog
    public Result<SystemNotification> editNotice(@RequestBody NoticeDTO noticeDTO) {
        systemNotificationService.editNotice(noticeDTO);
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 删除
     */
    @ApiOperation(value = "删除通知")
    @DeleteMapping("/deleteNotice")
    @LzhLog
    public Result deleteNotice(@RequestParam("id") Long id) {
        systemNotificationService.deleteNotice(id);
        return Result.success(null, ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }

    /**
     * 获取通知
     */
    @ApiOperation(value = "获取通知")
    @GetMapping("/getNotice")
    @LzhLog
    public Result<List<SystemNotification>> getNotice() {
        return Result.success(systemNotificationService.getNotice(), ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
    }
}
