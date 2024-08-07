package hope.smarteditor.user.controller;


import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hope.smarteditor.common.model.entity.Membership;
import hope.smarteditor.common.model.entity.Orders;
import hope.smarteditor.common.model.entity.PaymentOrder;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.utils.OrderUtil;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.mapper.MembershipMapper;
import hope.smarteditor.user.mapper.OrdersMapper;
import hope.smarteditor.user.mapper.UserMapper;
import hope.smarteditor.user.service.OrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;



import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
@Api(tags = "支付宝支付接口")
public class AliPayController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private MembershipMapper membershipMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @GetMapping("/pay")
    @ApiOperation(value = "支付宝支付接口")
    @LzhLog
    public String pay(PaymentOrder paymentOrder, @RequestParam String orderType) {
        AlipayTradePagePayResponse response;
        try {
            // 设置return_url
            String returnUrl = "http://192.168.50.187:5173/#/person";

            // 获取编码后的订单 ID
            String orderTypePrefix = orderType.equals("points") ? "points_" : "membership_";
            String encodedOrderId = paymentOrder.getEncodedOrderId(orderTypePrefix);

            // 发起API调用（以创建当面付收款二维码为例）
            response = Factory.Payment.Page()
                    .pay(paymentOrder.getOrderName(), encodedOrderId, String.valueOf(paymentOrder.getTotalPrice()), returnUrl);
        } catch (Exception e) {
            System.err.println("调用遭遇异常，原因：" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        return response.getBody();
    }


    @PostMapping("/notify")  // 注意这里必须是POST接口
    @LzhLog
    @ApiOperation(value = "支付宝异步回调接口")
    public String payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            System.out.println("requestParams = " + requestParams);
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
                System.out.println(name + " = " + request.getParameter(name));
            }
            String outTradeNo = params.get("out_trade_no");
            String[] parts = outTradeNo.split("_");
            String type = parts[0];
            System.out.println("type = " + type);
            String bookingId = parts[1];
            String name = params.get("subject");
            String price = params.get("total_amount");
            // 支付宝验签
            if (Factory.Payment.Common().verifyNotify(params)) {
                // 验签通过
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));
                ordersService.updateOrderStatus(Integer.parseInt(bookingId), "已支付");
                // 处理支付成功的逻辑
                if (type.equals("points")) {
                    // 更新订单为已支付
                    Orders orders1 = ordersMapper.selectById(Integer.parseInt(bookingId));
                    User user = userMapper.selectById(orders1.getUserId());
                    user.setMoney(user.getMoney() + orders1.getNum());
                    userMapper.updateById(user);
                } else if (type.equals("membership")) {
                    Orders orders = ordersService.getById(Integer.parseInt(bookingId));
                    String description = orders.getDescription();
                    User user = userMapper.selectById(orders.getUserId());
                    int months = orders.getNum(); // 会员月数，这里假设是月数，根据实际情况调整

                    Membership membership = new Membership();
                    QueryWrapper<Membership> membershipQueryWrapper = new QueryWrapper<>();
                    membershipQueryWrapper.eq("user_id", user.getId())
                            .orderByDesc("end_date")
                            .last("LIMIT 1");
                    membership = membershipMapper.selectOne(membershipQueryWrapper);
                    if (membership != null) {
                        // 如果该用户是会员，则更新会员时间累加
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(membership.getEndDate());
                        calendar.add(Calendar.MONTH, months);
                        membership.setEndDate(new Timestamp(calendar.getTimeInMillis()));
                    } else {
                        // 如果用户不是会员，则创建新的会员记录
                        membership = new Membership();
                        user.setLevel(1);  // 设置为会员
                        // 获取订单时间
                        Date orderTimeUtil = orders.getOrderTime();
                        Timestamp orderTime = new Timestamp(orderTimeUtil.getTime());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(orderTime);
                        calendar.add(Calendar.MONTH, months);
                        // 设置会员的起始日期和结束日期
                        membership.setStartDate(orderTime);
                        membership.setEndDate(new Timestamp(calendar.getTimeInMillis()));
                        membership.setUserId(user.getId());
                    }
                    userMapper.updateById(user);
                    // 插入或更新会员记录
                    membershipMapper.insert(membership);
                }
                // 从Redis移除相关的过期时间数据
                stringRedisTemplate.opsForZSet().remove("orders", bookingId);
            }
        }
        return "success";
    }
}

