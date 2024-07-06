package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class OcrVO implements Serializable {
    private String message;

    private String text;

    private String ocrImage;
}
