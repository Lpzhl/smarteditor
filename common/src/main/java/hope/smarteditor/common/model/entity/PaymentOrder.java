package hope.smarteditor.common.model.entity;

import hope.smarteditor.common.utils.OrderUtil;
import lombok.Data;

import java.math.BigDecimal;

@Data


public class PaymentOrder {
    private String orderName;
    private Integer orderId;
    private BigDecimal totalPrice;

    private String orderType;

    public String getEncodedOrderId(String orderTypePrefix) {
        return orderTypePrefix + this.orderId.toString();
    }
}
