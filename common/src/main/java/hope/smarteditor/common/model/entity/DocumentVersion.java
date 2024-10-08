package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * @TableName document_version
 */
@TableName(value ="document_version")
@Data
public class DocumentVersion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Double version;

    private String content;

    private String summary;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String username;

    private static final long serialVersionUID = 1L;
}
