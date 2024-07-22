package hope.smarteditor.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class OrdersVO implements Serializable {
    private Long id;

    /**
     *
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
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endOrderTime;
    /**
     * 订单状态 (待支付, 已支付, 已完成等)
     */
    private String status;

    /**
     * 订单描述
     */
    private String description;

    private Integer num;

}
