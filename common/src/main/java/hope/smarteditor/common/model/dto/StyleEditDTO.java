package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StyleEditDTO implements Serializable {
    private int styleId;       // 样式ID
    private String styleName;  // 样式名称
    private int ownerId;       // 所属者ID
    private List<StyleElementDTO> elements;  // 样式元素列表
}
