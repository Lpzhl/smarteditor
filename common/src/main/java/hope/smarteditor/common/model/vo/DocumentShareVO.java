package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DocumentShareVO implements Serializable {

    private List<Document> documents;
    private String category ;//种类(我的分享，我的接收)
}
