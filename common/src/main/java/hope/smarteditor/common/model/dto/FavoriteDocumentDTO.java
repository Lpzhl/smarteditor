package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FavoriteDocumentDTO implements Serializable {
    Long userId;
    Long documentId;
}
