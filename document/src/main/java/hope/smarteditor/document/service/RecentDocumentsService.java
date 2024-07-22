package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.RecentDocuments;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.DocumentInfoVO;
import hope.smarteditor.common.model.vo.RecentDocumentsVO;
import hope.smarteditor.common.result.Result;

import java.util.List;

/**
* @author LoveF
* @description 针对表【recent_documents】的数据库操作Service
* @createDate 2024-07-06 14:42:00
*/
public interface RecentDocumentsService extends IService<RecentDocuments> {

    void recordDocumentAccess(Long userId, Long documentId);

/*    List<RecentDocumentsVO> getRecentDocuments(Long userId);*/

    List<DocumentInfoVO> getRecentDocuments(Long userId);
}
