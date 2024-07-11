package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.User;
import lombok.Data;

import java.io.Serializable;
@Data
public class CollaborationVO implements Serializable {
    private User user;
    private Document document;
}
