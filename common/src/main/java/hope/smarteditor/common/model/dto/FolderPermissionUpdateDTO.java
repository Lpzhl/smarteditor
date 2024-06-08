package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FolderPermissionUpdateDTO implements Serializable {
    private Long id;
    private Integer Permission;
}
