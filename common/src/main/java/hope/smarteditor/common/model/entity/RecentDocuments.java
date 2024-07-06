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
@TableName(value ="recent_documents")
@Data
public class RecentDocuments implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private Integer userId;

    /**
     *
     */
    private Integer documentId;

    /**
     *
     */
    private Date accessTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public RecentDocuments(Long userId, Long docId, Date date) {
           this.userId = userId.intValue();

        this.documentId = docId.intValue();

        this.accessTime = date;
    }
}
