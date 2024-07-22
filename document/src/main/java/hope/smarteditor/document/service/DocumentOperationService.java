package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.DocumentOperation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author LoveF
* @description 针对表【document_operation】的数据库操作Service
* @createDate 2024-06-06 17:38:39
*/
public interface DocumentOperationService extends IService<DocumentOperation> {

    List<DocumentOperation> getDocumentLog(Long documentId);
}
