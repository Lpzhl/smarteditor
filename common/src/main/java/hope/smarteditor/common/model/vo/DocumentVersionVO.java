package hope.smarteditor.common.model.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DocumentVersionVO implements Serializable {
    private Long id;

    private Long documentId;

    private Double version;

    private String content;

    private String summary;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
    private String username;
}
