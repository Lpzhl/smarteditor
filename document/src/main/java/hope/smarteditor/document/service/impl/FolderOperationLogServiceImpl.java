package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.FolderOperationLog;
import hope.smarteditor.document.service.FolderOperationLogService;
import hope.smarteditor.document.mapper.FolderOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public List<FolderOperationLog> getFolderLog(Long folderId) {
        QueryWrapper<FolderOperationLog> folderOperationLogQueryWrapper = new QueryWrapper<>();

        folderOperationLogQueryWrapper.eq("folder_id",folderId);
        return folderOperationLogMapper.selectList(folderOperationLogQueryWrapper);
    }
}




