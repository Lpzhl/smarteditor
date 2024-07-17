package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserFolderInfoVO implements Serializable {
    private Long FolderId;

    private Long userId;

    private String FolderName;

    private String permissions;

    private List<DocumentInfoVO> documents;
}
