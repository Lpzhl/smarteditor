package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OrderBuyPointsDTO implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单类型 (buy_points: 购买积分, buy_membership: 购买会员)
     */
    private String orderType;

    /**
     * 订单金额
     */
    private Double amount;

    /**
     * 订单时间
     */
    private Date orderTime;


    /**
     * 订单状态 (待支付, 已支付, 已完成等)
     */
    private String status;

    /**
     * (购买的xx积分  xx个会员)
     */
    private Integer num;
    /**
     * 订单描述
     */
    private String description;
}
