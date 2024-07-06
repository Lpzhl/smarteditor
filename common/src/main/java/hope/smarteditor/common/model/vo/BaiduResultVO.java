package hope.smarteditor.common.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaiduResultVO implements Serializable {
    private String desc;
    private String href;
    private String kw;
    private int page;
    private String realUrl;
    private String site;
    private String title;
}
