package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.FolderOperationLog;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.FolderOperationLogVO;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.service.FolderOperationLogService;
import hope.smarteditor.document.mapper.FolderOperationLogMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    @Override
    public List<FolderOperationLogVO> getFolderLog(Long folderId) {
        // 创建查询条件，按操作时间倒序排列
        QueryWrapper<FolderOperationLog> folderOperationLogQueryWrapper = new QueryWrapper<>();
        folderOperationLogQueryWrapper.eq("folder_id", folderId).orderByDesc("operation_time");

        // 查询操作日志
        List<FolderOperationLog> folderOperationLogs = folderOperationLogMapper.selectList(folderOperationLogQueryWrapper);

        // 转换 FolderOperationLog 为 FolderOperationLogVO
        List<FolderOperationLogVO> folderOperationLogVOList = new ArrayList<>();
        for (FolderOperationLog folderOperationLog : folderOperationLogs) {
            FolderOperationLogVO logVO = new FolderOperationLogVO();
            BeanUtils.copyProperties(folderOperationLog, logVO);  // 复制共有属性

            // 获取用户头像
            User userInfo = userDubboService.getUserInfoByUserId(folderOperationLog.getUserId());
            logVO.setUserAvatar(userInfo.getAvatar());

            // 获取文档名称
            if (folderOperationLog.getDocumentId() != null) {
                logVO.setDocumentName(documentMapper.selectDocument(folderOperationLog.getDocumentId()).getName());
            }

            folderOperationLogVOList.add(logVO);
        }

        return folderOperationLogVOList;
    }

}




