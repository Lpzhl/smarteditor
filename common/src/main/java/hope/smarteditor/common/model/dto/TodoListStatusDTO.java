package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TodoListStatusDTO implements Serializable {
    private Long listId;
    /**
     * 完成状态 0未完成    1已完成
     */
    private Integer isCompleted;
}
