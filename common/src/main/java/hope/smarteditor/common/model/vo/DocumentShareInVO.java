package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DocumentShareInVO implements Serializable {

    private List<DocumentInfoVO> documents;
    private String category ;
}
