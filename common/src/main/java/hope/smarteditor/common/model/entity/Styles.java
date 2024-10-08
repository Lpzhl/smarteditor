package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 * 存储文档样式的基本信息
 * @TableName styles
 */
@TableName(value ="styles")
@Data
public class Styles implements Serializable {
    /**
     * 样式ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Integer styleId;

    /**
     * 样式名称
     */
    private String styleName;

    /**
     * 所属者ID
     */
    private Long userId;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
