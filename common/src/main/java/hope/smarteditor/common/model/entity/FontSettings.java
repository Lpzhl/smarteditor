package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 存储每个样式元素的字体格式、大小和行间距等设置
 * @TableName font_settings
 */
@TableName(value ="font_settings")
@Data
public class FontSettings implements Serializable {
    /**
     * 字体设置ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Integer settingId;

    /**
     * 关联的元素ID，外键
     */
    private Integer elementId;

    /**
     * 字体类型
     */
    private String fontFamily;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 行间距
     */
    private BigDecimal lineHeight;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
