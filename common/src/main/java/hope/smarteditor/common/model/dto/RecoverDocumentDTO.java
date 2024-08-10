package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecoverDocumentDTO implements Serializable {
    private Long documentId;
    private Long originalFolderId;
}
