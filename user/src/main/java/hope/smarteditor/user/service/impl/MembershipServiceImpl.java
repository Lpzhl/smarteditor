package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Membership;
import hope.smarteditor.user.service.MembershipService;
import hope.smarteditor.user.mapper.MembershipMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【membership】的数据库操作Service实现
* @createDate 2024-07-10 02:56:13
*/
@Service
public class MembershipServiceImpl extends ServiceImpl<MembershipMapper, Membership>
    implements MembershipService{

}




