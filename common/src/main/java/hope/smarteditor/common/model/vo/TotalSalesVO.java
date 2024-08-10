package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TotalSalesVO implements Serializable {
    private String timeCategory; // 时间分类 (近七天、今天、一个月、总共)
    private double pointsSales;  // 积分销售额
    private double membershipSales; // 会员销售额
    private Integer pointsOrderCount;
    private Integer membershipOrderCount;
    private double totalSales; // 总销售额

}
