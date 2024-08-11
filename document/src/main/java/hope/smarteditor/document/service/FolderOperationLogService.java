package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.FolderOperationLog;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.FolderOperationLogVO;

import java.util.List;

/**
* @author LoveF
* @description 针对表【folder_operation_log】的数据库操作Service
* @createDate 2024-06-09 21:01:37
*/
public interface FolderOperationLogService extends IService<FolderOperationLog> {

    List<FolderOperationLogVO> getFolderLog(Long folderId);
}
