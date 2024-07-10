package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
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
    private Integer amount;

    /**
     * 订单时间
     */
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
