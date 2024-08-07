package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 *
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 订单状态 (待支付, 已支付, 已完成等)
     */
    private String status;

    /**
     * 订单描述
     */
    private String description;

    private Integer num;

    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
