package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName permissions
 */
@TableName(value ="permissions")
@Data
public class Permissions implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long permissionId;

    private String permissionName;

    private Date createdAt;

    private static final long serialVersionUID = 1L;
}