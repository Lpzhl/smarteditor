package hope.smarteditor.document.service;

import hope.smarteditor.common.model.dto.*;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.UserFolderInfoVO;

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

    boolean deleteDocument(Long documentId);

    boolean moveDocument(MoveDocumentDTO moveDocumentDTO,Long userId);

    List<UserFolderInfoVO> getFolderDocument(Long userId);

    boolean moveDocumentToFolder(MoveDocumentToFolderDTO moveDocumentToFolderDTO, Long userId);
}
