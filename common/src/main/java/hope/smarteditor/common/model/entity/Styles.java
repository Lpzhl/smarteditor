package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Integer ownerId;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
