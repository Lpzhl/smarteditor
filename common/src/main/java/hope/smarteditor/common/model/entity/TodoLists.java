package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Data;

/**
 *
 * @TableName todo_lists
 */
@TableName(value ="todo_lists")
@Data
public class TodoLists implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long listId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 清单名称
     */
    private String listName;

    /**
     * 完成状态 0未完成    1已完成
     */
    private Integer isCompleted;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
