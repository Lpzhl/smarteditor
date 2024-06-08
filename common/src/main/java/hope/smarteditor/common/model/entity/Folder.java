package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName folder
 */
@TableName(value ="folder")
@Data
public class Folder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    @TableLogic
    private Integer isDeleted;

    private Date createTime;

    private Date updateTime;

    private String permissions;

    private static final long serialVersionUID = 1L;
}