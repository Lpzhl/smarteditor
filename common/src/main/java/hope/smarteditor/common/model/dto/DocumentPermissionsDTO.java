package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentPermissionsDTO implements Serializable {
    private Long documentId;

    private Long userId;

    private Long permissionId;
}
