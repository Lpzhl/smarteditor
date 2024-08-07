package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.DocumentVersion;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.DocumentVersionVO;

import java.util.List;

/**
* @author LoveF
* @description 针对表【document_version】的数据库操作Service
* @createDate 2024-06-06 17:41:49
*/
public interface DocumentVersionService extends IService<DocumentVersion> {

    List<DocumentVersionVO> getDocumentVersion(Long documentId);

    void rollbackDocumentVersion(Long documentId, Long versionId, Long userId);

}
