package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.NoticeDTO;
import hope.smarteditor.common.model.entity.SystemNotification;
import hope.smarteditor.user.service.SystemNotificationService;
import hope.smarteditor.user.mapper.SystemNotificationMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author LoveF
* @description 针对表【system_notification(系统通知表，发布各种促销活动和系统通知)】的数据库操作Service实现
* @createDate 2024-08-13 07:44:58
*/
@Service
public class SystemNotificationServiceImpl extends ServiceImpl<SystemNotificationMapper, SystemNotification>
    implements SystemNotificationService{

    @Resource
    private SystemNotificationMapper systemNotificationMapper;
    @Override
    public SystemNotification publishNotice(NoticeDTO noticeDTO) {
        SystemNotification systemNotification = new SystemNotification();
        BeanUtils.copyProperties(noticeDTO,systemNotification);
        systemNotificationMapper.insert(systemNotification);
        return systemNotification;
    }

    @Override
    public SystemNotification editNotice(NoticeDTO noticeDTO) {
        SystemNotification systemNotification = new SystemNotification();
        BeanUtils.copyProperties(noticeDTO,systemNotification);
        systemNotificationMapper.updateById(systemNotification);
        return systemNotification;
    }

    @Override
    public void deleteNotice(Long id) {
        systemNotificationMapper.deleteById(id);
    }

    @Override
    public List<SystemNotification> getNotice() {
        return systemNotificationMapper.selectList(null);
    }
}




