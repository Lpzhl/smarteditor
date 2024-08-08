package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.FolderOperationLog;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.service.FolderOperationLogService;
import hope.smarteditor.document.mapper.FolderOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author LoveF
* @description 针对表【folder_operation_log】的数据库操作Service实现
* @createDate 2024-06-09 21:01:37
*/
@Service
public class FolderOperationLogServiceImpl extends ServiceImpl<FolderOperationLogMapper, FolderOperationLog>
    implements FolderOperationLogService{

    @Autowired
    private FolderOperationLogMapper folderOperationLogMapper;
    @Resource
    private DocumentMapper documentMapper;

    @Override
    public List<FolderOperationLog> getFolderLog(Long folderId) {
        QueryWrapper<FolderOperationLog> folderOperationLogQueryWrapper = new QueryWrapper<>();

        folderOperationLogQueryWrapper.eq("folder_id",folderId).orderByDesc("operation_time");
        List<FolderOperationLog> folderOperationLogs = folderOperationLogMapper.selectList(folderOperationLogQueryWrapper);
        for (FolderOperationLog folderOperationLog : folderOperationLogs) {
            if(folderOperationLog.getDocumentId()!=null){
                folderOperationLog.setDocumentName(documentMapper.selectDocument(folderOperationLog.getDocumentId()).getName());
            }
        }
        return folderOperationLogs;
    }
}




