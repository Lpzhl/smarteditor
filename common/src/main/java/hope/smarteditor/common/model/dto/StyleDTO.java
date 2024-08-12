package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StyleDTO implements Serializable {
    /**
     * 样式ID
     */
    private Integer styleId;

    /**
     * 样式名称
     */
    private String styleName;

    /**
     * 所属者ID
     */
    private Integer userId;

    /**
     * 文字样式集合
     */
    private List<FontSettingsDTO> fontSettings;

}
