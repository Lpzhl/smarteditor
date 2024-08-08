package hope.smarteditor.document.service;


import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.dto.TemplateDocumentUpdateDTO;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.common.model.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.multipart.MultipartFile;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
* @author lzh
* @description 针对表【document】的数据库操作Service
* @createDate 2024-05-22 14:26:07
*/
public interface DocumentService extends IService<Document> {

    String uploadFile(MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, Exception;

    Document saveDocument(DocumentUploadDTO documentUploadDTO);

    boolean deleteDocument(Long documentId);

    Document updateDocument(Long documentId, DocumentUpdateDTO documentUpdateDTO);


    void setDocumentVisibility(Long documentId);

    List<DocumentInfoVO> getDeletedDocuments(Long userId);


    List<DocumentUserPermisssVO> getParticipants(Long documentId);

    List<Document> searchDocumentsByName(String keyword, Long userId);

    Document getDocumentById(Long userId,Long documentId);

    List<Folder> searchDocumentsByCreator(String keyword, Long userId);

    void setDocumentAsTemplate(Long documentId,Long userId);

    List<DocumentShareInVO> getDocumentShare(Long userId);

    SearchVO searchDocumentsByContent(String keyword, Long userId);

    void deleteDocumentBatch(List<Long> documentIds);

    void renameDocument(Long documentId, String newName,Long userId);

    List<TemplateDocument> getTemplateDocument(Long userId);

    List<TemplateDocument> getTemplateShow();

    void editTemplate(TemplateDocumentUpdateDTO templateDocument);

    void saveLog(Long documentId,DocumentUpdateDTO documentUpdateDTO);
    void createLog(Long documentId,Long userId);

    void restoreDeletedDocument(Long documentId);
}
