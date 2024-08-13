package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 系统通知表，发布各种促销活动和系统通知
 * @TableName system_notification
 */
@TableName(value ="system_notification")
@Data
public class SystemNotification implements Serializable {
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long notificationId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
