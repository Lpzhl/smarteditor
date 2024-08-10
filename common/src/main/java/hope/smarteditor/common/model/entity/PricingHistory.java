package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName Pricing_History
 */
@TableName(value ="Pricing_History")
@Data
public class PricingHistory implements Serializable {
    /**
     *主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  价格表的id
     */
    private Long pricingId;

    /**
     *  旧价格
     */
    private BigDecimal oldPrice;

    /**
     * 新价格
     */
    private BigDecimal newPrice;

    /**
     * 修改时间
     */
    private Date changedAt;

    /**
     * 修改人
     */
    private Long changedBy;

    /**
     * 旧值
     */
    private Integer oldValue;

    /**
     * 新值
     */
    private Integer newValue;

    /**
     * 描述
     */
    private String describe;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
