package hope.smarteditor.api;



import hope.smarteditor.common.model.entity.Document;

import java.util.List;

/**
 * author lzh
 */
public interface DocumentDubboService  {

    /**
     * 获取用户的所有文档信息
     */
    List<Document> getUserAllDocumentInfo(String userId);

}
