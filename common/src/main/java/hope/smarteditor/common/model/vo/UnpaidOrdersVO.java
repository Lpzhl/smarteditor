package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Orders;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UnpaidOrdersVO implements Serializable {
    private String type; // 订单类型
    private int unpaidOrderCount; // 未支付订单数
    private double unpaidOrderAmount; // 未支付订单金额
    private List<Orders> unpaidOrders; // 未支付订单列表

}
