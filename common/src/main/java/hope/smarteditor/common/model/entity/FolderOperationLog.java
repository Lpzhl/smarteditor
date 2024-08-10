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
 *
 * @TableName folder_operation_log
 */
@TableName(value ="folder_operation_log")
@Data
public class FolderOperationLog implements Serializable {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文件夹ID
     */
    private Long folderId;

    /**
     * 文档ID，若涉及文件操作则记录文档ID
     */
    private Long documentId;

    /**
     * 文档名，若涉及文件操作则记录文档名
     */
    private String documentName;

    /**
     * 操作类型（创建、插入、删除、更新）
     */
    private String operation;

    /**
     * 操作时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
