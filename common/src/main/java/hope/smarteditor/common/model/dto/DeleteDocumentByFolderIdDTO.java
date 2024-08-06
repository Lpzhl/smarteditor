package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeleteDocumentByFolderIdDTO implements Serializable {
    private Long folderId;
    private List<Long> documentIds;
}
