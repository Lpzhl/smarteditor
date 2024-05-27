package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * @TableName user_document_like
 */
@TableName(value ="user_document_like")
@Data
public class UserDocumentLike implements Serializable {
    private Long id;

    private Long userId;

    private Long documentId;

    private static final long serialVersionUID = 1L;
}