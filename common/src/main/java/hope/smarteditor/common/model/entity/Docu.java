package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @TableName docu
 */
@TableName(value ="docu")
@Data
public class Docu implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 文献名称
     */
    private String name;

    /**
     * 文献内容
     */
    private String context;

    /**
     * 文献关键字
     */
    private String documentKey;

    /**
     * 专业
     */
    private String profession;

    /**
     * 学科
     */
    private String subject;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
