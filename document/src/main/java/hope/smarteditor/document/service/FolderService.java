package hope.smarteditor.document.service;

import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.DocumentInfoVO;
import hope.smarteditor.common.model.vo.UserFolderInfoVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author LoveF
* @description 针对表【folder】的数据库操作Service
* @createDate 2024-06-08 14:02:31
*/
public interface FolderService extends IService<Folder> {

    boolean createFolder(FolderDTO folderDTO);

    boolean deleteFolder(Long folderId);

    boolean updateFolder(FolderUpdateDTO folderDTO,Long userId);

    boolean setFolderPermission(FolderPermissionUpdateDTO folderDTO);

    boolean createDocument(Long folderId, Document document, Long userId);

    boolean deleteDocument(Long documentId,Long folderId,Long userId);

    boolean moveDocument(MoveDocumentDTO moveDocumentDTO,Long userId);

    List<UserFolderInfoVO> getFolderDocument(Long userId);

    boolean moveDocumentToFolder(MoveDocumentToFolderDTO moveDocumentToFolderDTO, Long userId);

    List<Folder> searchFoldersByName(String keyword, Long userId);

    List<DocumentInfoVO> getDocumentByFolderId(Long folderId);

    String deleteRecentDocument(Long documentId, Long userId);

    Boolean deleteDocumentByFolderId(DeleteDocumentByFolderIdDTO deleteDocumentByFolderIdDTO,Long userId);

    Boolean recoverDocument(RecoverDocumentDTO recoverDocumentDTO,Long userId);
}
