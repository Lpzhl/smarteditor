package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Membership;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.user.service.MembershipService;
import hope.smarteditor.user.mapper.MembershipMapper;
import hope.smarteditor.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
* @author LoveF
* @description 针对表【membership】的数据库操作Service实现
* @createDate 2024-07-10 02:56:13
*/
@Service
public class MembershipServiceImpl extends ServiceImpl<MembershipMapper, Membership>
    implements MembershipService{

    @Autowired
    private MembershipMapper membershipMapper;

    @Autowired
    private UserService userService;


    /**
     * 定时任务 每天凌晨0点执行一次 检测用户的会员是否到期
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkMembershipExpiry() {
        List<Membership> expiredMemberships = membershipMapper.findExpiredMemberships();

        for (Membership membership : expiredMemberships) {
            User user = userService.getById(membership.getUserId());
            if (user != null) {
                user.setLevel(0);
                userService.updateById(user);
            }
        }
    }

}




