package hope.smarteditor.api;



import hope.smarteditor.common.model.dto.FavoriteDocumentDTO;
import hope.smarteditor.common.model.dto.FavoriteTemplateDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.vo.FavoriteDocumentVO;
import hope.smarteditor.common.model.vo.FavoriteTemplateVO;
import hope.smarteditor.common.result.Result;

import java.util.List;

/**
 * author lzh
 */

public interface DocumentDubboService  {

    /**
     * 获取用户的所有文档信息
     */
    List<Document> getUserAllDocumentInfo(String userId);

    /**
     * 获取文档信息
     * @param documentId
     * @return
     */
    Document getDocumentById(Long documentId);

    List<FavoriteDocumentVO> getUserFavoriteDocuments(Long userId);


    boolean toggleFavoriteDocument(FavoriteDocumentDTO favoriteDocumentDTO);

    boolean toggleFavoriteTemplate(FavoriteTemplateDTO favoriteTemplateDTO);

    List<FavoriteTemplateVO> getUserFavoriteTemplates(Long userId);

    /**
     * 文档点赞和取消点赞
     */

    boolean likeDocument(Long documentId, Long userId);

    /**
     * 用户创建文件夹
     */
    boolean createFolder(String folderName, Long userId);
}
