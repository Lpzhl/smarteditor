package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import lombok.Data;

import java.io.Serializable;

@Data
public class RecentDocumentsVO implements Serializable {
    private Document document;  //文档
    private String category;    // 时间类型
}
