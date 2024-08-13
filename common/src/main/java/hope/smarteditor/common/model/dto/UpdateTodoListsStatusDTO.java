package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UpdateTodoListsStatusDTO implements Serializable {
    private List<TodoListStatusDTO> todoListStatusDTOList;
}
