package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentVO implements Serializable {

    private String id;
    private String name;
    private String content;
    private String summary;
    private String subject;
    private String category;

}
