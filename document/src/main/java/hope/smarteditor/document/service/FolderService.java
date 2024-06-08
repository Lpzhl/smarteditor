package hope.smarteditor.document.service;

import hope.smarteditor.common.model.dto.FolderDTO;
import hope.smarteditor.common.model.dto.FolderPermissionUpdateDTO;
import hope.smarteditor.common.model.dto.FolderUpdateDTO;
import hope.smarteditor.common.model.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author LoveF
* @description 针对表【folder】的数据库操作Service
* @createDate 2024-06-08 14:02:31
*/
public interface FolderService extends IService<Folder> {

    boolean createFolder(FolderDTO folderDTO);

    boolean deleteFolder(Long folderId);

    boolean updateFolder(FolderUpdateDTO folderDTO);

    boolean setFolderPermission(FolderPermissionUpdateDTO folderDTO);

    boolean createDocument(Long folderId, Long documentId);

    boolean deleteDocument(Long documentId);
}
