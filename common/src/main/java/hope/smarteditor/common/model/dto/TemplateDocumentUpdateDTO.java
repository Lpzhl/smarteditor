package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TemplateDocumentUpdateDTO implements Serializable  {
    private Long id;
    private String name;
    private String content;
    private String summary;
    private Integer type;
    private String label;
    private Integer status;
    private String subject;
    private String category;
    private Long userId;
}
