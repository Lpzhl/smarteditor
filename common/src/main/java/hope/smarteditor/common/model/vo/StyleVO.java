package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StyleVO implements Serializable {
    private int styleId;       // 样式ID
    private String styleName;  // 样式名称
    private Long userId;       // 所属者ID
    private List<FontSettingsVO> fontSettingsVOS;  // 样式中的元素列表

}
