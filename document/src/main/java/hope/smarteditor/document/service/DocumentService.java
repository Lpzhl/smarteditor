package hope.smarteditor.document.service;


import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.dto.DocumentUpdateDTO;
import hope.smarteditor.common.model.dto.DocumentUploadDTO;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.DocumentUserPermisssVO;
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

    List<Document> getDeletedDocuments(Long userId);


    List<DocumentUserPermisssVO> getParticipants(Long documentId);
}
