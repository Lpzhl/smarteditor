package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 存储每种样式中各级标题和正文的基本信息
 * @TableName style_elements
 */
@TableName(value ="style_elements")
@Data
public class StyleElements implements Serializable {
    /**
     * 元素ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Integer elementId;

    /**
     * 关联的样式ID，外键
     */
    private Integer styleId;

    /**
     * 元素类型（一级标题、二级标题、三级标题、正文）
     */
    private String elementType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
