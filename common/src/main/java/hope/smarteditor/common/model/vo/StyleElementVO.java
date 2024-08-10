package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StyleElementVO implements Serializable {
    private int elementId;       // 元素ID
    private String elementType;  // 元素类型（一级标题、二级标题、三级标题、正文）
    private FontSettingVO fontSettings;  // 字体设置
}
