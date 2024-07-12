package hope.smarteditor.user.controller;


import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
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
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
@Api(tags = "֧����֧���ӿ�")
public class AliPayController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private MembershipMapper  membershipMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @GetMapping("/pay")
    @ApiOperation(value = "֧����֧���ӿ�")
    @LzhLog
    public String pay(PaymentOrder paymentOrder, @RequestParam String orderType) {
        AlipayTradePagePayResponse response;
        try {
            // ����return_url
            String returnUrl = "http://8qeqvk.natappfree.cc/alipay/payment-success";

            // ��ȡ�����Ķ��� ID
            String orderTypePrefix = orderType.equals("points") ? "points_" : "membership_";
            String encodedOrderId = paymentOrder.getEncodedOrderId(orderTypePrefix);

            // ����API���ã��Դ������渶�տ��ά��Ϊ����
            response = Factory.Payment.Page()
                    .pay(paymentOrder.getOrderName(), encodedOrderId, String.valueOf(paymentOrder.getTotalPrice()), returnUrl);
        } catch (Exception e) {
            System.err.println("���������쳣��ԭ��" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        return response.getBody();
    }


    @PostMapping("/notify")  // ע�����������POST�ӿ�
    @LzhLog
    @ApiOperation(value = "֧�����첽�ص��ӿ�")
    public String payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========֧�����첽�ص�========");

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
            // ֧������ǩ
            if (Factory.Payment.Common().verifyNotify(params)) {
                // ��ǩͨ��
                System.out.println("��������: " + params.get("subject"));
                System.out.println("����״̬: " + params.get("trade_status"));
                System.out.println("֧��������ƾ֤��: " + params.get("trade_no"));
                System.out.println("�̻�������: " + params.get("out_trade_no"));
                System.out.println("���׽��: " + params.get("total_amount"));
                System.out.println("�����֧����Ψһid: " + params.get("buyer_id"));
                System.out.println("��Ҹ���ʱ��: " + params.get("gmt_payment"));
                System.out.println("��Ҹ�����: " + params.get("buyer_pay_amount"));
                ordersService.updateOrderStatus(Integer.parseInt(bookingId), "�����");
                // ����֧���ɹ����߼�
                if (type.equals("points")) {
                    // ���¶���δ��֧��
                    Orders orders1 = ordersMapper.selectById(Integer.parseInt(bookingId));
                    User user = userMapper.selectById(orders1.getUserId());
                    user.setMoney(user.getMoney() + orders1.getNum());
                    userMapper.updateById(user);

                    // ����WebSocket��Ϣ
                    //String message = "֧���ɹ��������ţ�" + bookingId;
                    //myWebSocketHandler.sendMessage(message);
              /*  // ����֧���ɹ���Ϣ�� Redis
                stringRedisTemplate.convertAndSend("paymentSuccess", "֧���ɹ��������ţ�" + bookingId);*/
                    if (type.equals("membership")) {
                        Membership membership = new Membership();
                        Orders orders = ordersService.getById(Integer.parseInt(bookingId));
                        String description = orders.getDescription();

                        int days = orders.getNum(); // ��Ա�������������������������ʵ���������

                        // ��ȡ����ʱ��
                        Timestamp orderTime = (Timestamp) orders.getOrderTime();

                        // ʹ�� Calendar �����������ڲ���
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(orderTime);
                        calendar.add(Calendar.DAY_OF_MONTH, days); // ��������

                        // ���û�Ա����ʼ���ںͽ�������
                        membership.setStartDate(orderTime);
                        membership.setEndDate(new Timestamp(calendar.getTimeInMillis()));

                        // �����Ա��¼
                        membershipMapper.insert(membership);
                    }
                    // ��Redis�Ƴ���صĹ���ʱ������
                    stringRedisTemplate.opsForZSet().remove("orders", bookingId);
                }
            }

        }
        return "success";
    }

}
