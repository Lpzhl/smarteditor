package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @TableName font_settings
 */
@TableName(value ="font_settings")
@Data
public class FontSettings implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型
     */
    private String name;

    /**
     * 样式id
     */
    private Long stylesId;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
