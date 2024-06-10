package hope.smarteditor.common.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class MoveDocumentDTO implements Serializable {
    private Long sourceFolderId;

    private Long documentId;

    private Long targetFolderId;
}
