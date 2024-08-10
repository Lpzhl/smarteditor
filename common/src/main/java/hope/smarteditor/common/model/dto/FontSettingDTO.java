package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FontSettingDTO implements Serializable {
    private int styleId;       // 样式ID
    private String fontFamily;  // 字体类型
    private int fontSize;       // 字体大小
    private float lineHeight;   // 行间距

}
