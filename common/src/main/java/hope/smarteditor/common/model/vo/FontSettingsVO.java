package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FontSettingsVO implements Serializable {
    private Long id;
    /**
     * 样式ID
     */
    private Long stylesId;

    /**
     * 名称
     */
    private String name;

    /**
     * 文字样式
     */
    private String fontFamily;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 行间距
     */
    private Double lineHeight;
}
