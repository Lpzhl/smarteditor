package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CollaborationDTO implements Serializable {
    private Long userId;
    private Long documentId;
}
