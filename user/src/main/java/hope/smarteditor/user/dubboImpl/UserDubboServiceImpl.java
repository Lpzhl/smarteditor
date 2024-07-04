package hope.smarteditor.user.dubboImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.user.mapper.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * author lzh
 */

@Service
@DubboService(version = "1.0.0", group = "user",interfaceClass = UserDubboService.class)
public class UserDubboServiceImpl implements UserDubboService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getUserNameByUserId(Long userId) {
        LambdaQueryWrapper<User> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(User::getId, userId);
        User user = userMapper.selectOne(lambdaQuery);
        return user.getUsername();
    }

    @Override
    public List<User> getUserInfoByUserId(List<Long> userIds) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIds);
        List<User> users = userMapper.selectList(userQueryWrapper);
        for (User user : users) {
            user.setPassword("**********");
        }
        return users;
    }

}
