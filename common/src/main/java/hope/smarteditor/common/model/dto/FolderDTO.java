package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FolderDTO implements Serializable {
    private Long userId;

    private String name;

}