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
 * @TableName deleted_info
 */
@TableName(value ="deleted_info")
@Data
public class DeletedInfo implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 删除者
     */
    private Long userId;

    /**
     * 删除的文档Id
     */
    private Long documentId;

    /**
     * 文档所在文件夹id
     */
    private Long originalFolderId;

    /**
     * 文件夹id
     */
    private Long folderId;

    /**
     * 删除时间
     */
    private Date deletionTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
