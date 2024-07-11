package hope.smarteditor.common.model.vo;

import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.User;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RecentDocumentsVO implements Serializable {
    private List<DocumentInfoVO> documentInfoVOList;  //文档
    private String category;    // 时间类型
}
