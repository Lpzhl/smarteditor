package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StyleElementDTO implements Serializable {
    private int styleId;       // 样式ID
    private int elementId;       // 元素ID
    private String elementType;  // 元素类型（一级标题、二级标题、三级标题、正文）
    private FontSettingDTO fontSettings;  // 字体设置

}
