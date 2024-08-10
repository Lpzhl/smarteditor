package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PriceUpdateDTO implements Serializable {
    private Long id; // 对应价格表中的记录ID
    private Integer orderValue; // 积分数量或会员时长（以月为单位）
    private Integer newValue; // 积分数量或会员时长（以月为单位）
    private BigDecimal originalPrice; // 原始价格
    private BigDecimal price; // 新价格
    private Long chanceId; // 修改人id
}
