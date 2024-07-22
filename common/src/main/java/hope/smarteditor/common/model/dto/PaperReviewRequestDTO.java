package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 解决论文评审超长文本参数请求失败
 */
@Data
public class PaperReviewRequestDTO implements Serializable {
    private String text;
}
