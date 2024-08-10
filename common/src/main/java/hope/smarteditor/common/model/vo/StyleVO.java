package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StyleVO implements Serializable {
    private int styleId;       // 样式ID
    private String styleName;  // 样式名称
    private int ownerId;       // 所属者ID
    private List<StyleElementVO> elements;  // 样式中的元素列表

}
