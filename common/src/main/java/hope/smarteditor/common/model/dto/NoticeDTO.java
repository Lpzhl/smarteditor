package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NoticeDTO implements Serializable {
    private Long notificationId;
    private String title;
    private String type;
    private String content;
    private Long userId;
}
