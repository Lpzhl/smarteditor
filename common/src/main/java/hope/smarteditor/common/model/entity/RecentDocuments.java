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
 * @TableName recent_documents
 */
/**
 * @TableName recent_documents
 */
@TableName(value ="recent_documents")
@Data
public class RecentDocuments implements Serializable {
    private Long id;

    private Long userId;

    private Long documentId;

    private Date accessTime;

    private static final long serialVersionUID = 1L;
}
