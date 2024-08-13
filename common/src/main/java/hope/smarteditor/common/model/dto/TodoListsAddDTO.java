package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TodoListsAddDTO implements Serializable {
    /**
     * 清单名称
     */
    private String listName;

    private Long documentId;
}
