package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FontSettingVO implements Serializable {
    private String fontFamily;  // 字体类型
    private int fontSize;       // 字体大小
    private BigDecimal lineHeight;   // 行间距

}
