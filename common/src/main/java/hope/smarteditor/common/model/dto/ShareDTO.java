package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShareDTO implements Serializable {
    private Long id;
    private Integer validDays;
    private String editPermission;
}
