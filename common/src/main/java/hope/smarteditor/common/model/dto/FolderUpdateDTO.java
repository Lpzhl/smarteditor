package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FolderUpdateDTO implements Serializable {
    private Long id;
    private String name;
}