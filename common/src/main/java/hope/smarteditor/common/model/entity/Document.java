package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @TableLogic
    private Integer isDeleted;

    private Integer likeCount;

    private Integer visibility;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}