package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    private Long id;

    private String username;

    private String password;

    private String email;

    private Integer level;

    private Integer money;

    //@TableField("create_time")
    private Date createTime;
    //@TableField("update_time")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}