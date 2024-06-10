package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MoveDocumentToFolderDTO implements Serializable {
    private Long documentId;

    private Long folderId;
}
