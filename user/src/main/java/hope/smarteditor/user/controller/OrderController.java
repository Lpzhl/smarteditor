package hope.smarteditor.user.controller;

import hope.smarteditor.common.model.dto.OrderBuyPointsDTO;
import hope.smarteditor.common.model.dto.OrderBuyVipDTO;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
@Api(tags = "订单接口")
public class OrderController {


    @Autowired
    private OrdersService ordersService;



    /**
     * 创建购买积分订单
     */
    @PostMapping("/createOrderBuyPoints")
    @LzhLog
    @ApiOperation(value = "创建购买积分订单")
    public Result createOrder(@RequestBody OrderBuyPointsDTO orderBuyPointsDTO) {
        return ordersService.createOrderBuyPoints(orderBuyPointsDTO);
    }

    /**
     * 创建购买会员的订单
     */
     @PostMapping("/createOrderBuyVip")
     @ApiOperation(value = "创建购买会员的订单")
     @LzhLog
     public Result createOrderBuyVip(@RequestBody OrderBuyVipDTO orderBuyVipDTO) {
         return ordersService.createOrderBuyVip(orderBuyVipDTO);
     }

    /**
     * 获取用户所有订单信息
     */
    @PostMapping("/getUserAllOrder")
    @ApiOperation(value = "获取用户所有订单信息")
    public Result getUserAllOrder(HttpServletRequest request) {
        String userId = request.getHeader("userId");
        return ordersService.getUserAllOrder(userId);
    }

    /**
     * 取消订单
     */
     @PostMapping("/cancelOrder/{orderId}")
     @ApiOperation(value = "取消订单")
     @LzhLog
     public Result cancelOrder(@PathVariable ("orderId") String orderId) {
         return ordersService.cancelOrder(orderId);
     }
}
