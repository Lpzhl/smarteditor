package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Folder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchVO implements Serializable {
    private List<Document> documents;
    private List<Folder> folders;
}
