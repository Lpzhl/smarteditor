package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName membership
 */
@TableName(value ="membership")
@Data
public class Membership implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long userId;

    /**
     * 会员起始日期
     */
    private Date startDate;

    /**
     * 会员到期日期
     */
    private Date endDate;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
