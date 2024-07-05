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
 *
 * @TableName share
 */
@TableName(value ="share")
@Data
public class Share implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档id
     */
    private Long documentId;

    /**
     * 文件夹id
     */
    private Long folderId;

    /**
     * 链接
     */
    private String link;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 编辑权限
     */
    private String editPermission;

    /**
     * 是否永久有效
     */
    private Integer permanent;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
