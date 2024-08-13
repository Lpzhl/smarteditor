package hope.smarteditor.user.service;

import hope.smarteditor.common.model.dto.NoticeDTO;
import hope.smarteditor.common.model.entity.SystemNotification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author LoveF
* @description 针对表【system_notification(系统通知表，发布各种促销活动和系统通知)】的数据库操作Service
* @createDate 2024-08-13 07:44:58
*/
public interface SystemNotificationService extends IService<SystemNotification> {

    SystemNotification publishNotice(NoticeDTO noticeDTO);

    SystemNotification editNotice(NoticeDTO noticeDTO);


    void deleteNotice(Long id);

    List<SystemNotification> getNotice();

}
