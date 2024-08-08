package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.constant.UserInfoConstant;
import hope.smarteditor.common.model.entity.SignIn;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.user.mapper.UserMapper;
import hope.smarteditor.user.service.SignInService;
import hope.smarteditor.user.mapper.SignInMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
* @author LoveF
* @description 针对表【sign_in】的数据库操作Service实现
* @createDate 2024-07-06 16:54:22
*/
@Service
public class SignInServiceImpl extends ServiceImpl<SignInMapper, SignIn>
    implements SignInService{

    @Autowired
    private SignInMapper signInMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String sign(Long userId) {
        // 插入签到记录
        SignIn signIn = new SignIn();
        signIn.setUserId(userId);
        signInMapper.insert(signIn);

        // 查询用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", userId);
        User user = userMapper.selectOne(userQueryWrapper);

        if (user != null) {
            // 增加用户余额
            int upMoney = (user.getLevel() == UserInfoConstant.VIP_USER) ? UserInfoConstant.VIP_USER_UP_MONEY : UserInfoConstant.USER_UP_MONEY;
            user.setMoney(user.getMoney() + upMoney);

            // 更新用户信息
            userMapper.updateById(user);
        }

        return MessageConstant.SUCCESSFUL;
    }


    /**
     * 检查用户是否已经签到了
     * @param userId
     * @return
     */
    @Override
    public boolean checkSign(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        //  使用redis优化性能  redis保存用户签到的记录 有效时间为当前时间距离明天的时间差
        QueryWrapper<SignIn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .between("sign_in_date", startOfDay, endOfDay);

        int count = signInMapper.selectCount(queryWrapper);
        return count > 0;
    }



}




