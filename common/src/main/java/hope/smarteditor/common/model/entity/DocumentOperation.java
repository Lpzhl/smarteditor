package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName document_operation
 */
@TableName(value ="document_operation")
@Data
public class DocumentOperation implements Serializable {
    private Long id;

    private Long documentId;

    private Long userId;

    private String operation;

    private String description;

    private Date operationTime;

    private static final long serialVersionUID = 1L;
}