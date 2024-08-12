package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TodoListsVO implements Serializable {

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

}
