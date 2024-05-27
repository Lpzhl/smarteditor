package hope.smarteditor.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.TemplateDocument;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FavoriteTemplateVO implements Serializable {
    private Long id;

    private Long userId;

    private TemplateDocument document;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
