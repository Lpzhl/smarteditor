package hope.smarteditor.common.model.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
@Data
public class DocumentFolderDTO implements Serializable {
    private Long folderId;
    private DocumentUploadDTO documentUploadDTO;
}
