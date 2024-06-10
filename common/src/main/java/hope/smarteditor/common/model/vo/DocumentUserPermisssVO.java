package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.User;
import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentUserPermisssVO implements Serializable {
    private User user;

    private String permission;  //职责
}
