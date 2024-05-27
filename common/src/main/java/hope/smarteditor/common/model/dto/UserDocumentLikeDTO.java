package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDocumentLikeDTO implements Serializable {
    private Long userId;

    private Long documentId;
}
