package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * @TableName document
 */
@TableName(value ="document")
@Data
public class Document implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String content;

    private String summary;

    private Integer type;

    private String label;

    private Integer status;

    private String createTime;

    private String updateTime;

    private static final long serialVersionUID = 1L;
}