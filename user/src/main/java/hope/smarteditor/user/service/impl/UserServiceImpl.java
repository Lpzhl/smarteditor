package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.UserLoginDTO;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.user.service.UserService;
import hope.smarteditor.user.mapper.UserMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author LoveF
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-05-13 20:51:50
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userLoginDTO.getUsername())
                .eq("password", DigestUtils.md5Hex(userLoginDTO.getPassword()));
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateUser(User user) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(User::getUsername, user.getUsername());
        int i = userMapper.update(user, updateWrapper);
        return i > 0;
    }

    @Override
    public boolean register(User user) {

        // 先检查数据库中是否已经存在 username 如果存在则直接返回 false 否则进行注册
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", user.getUsername());
        User user1 = userMapper.selectOne(queryWrapper);
        if (user1!=null) {
            return false;
        }
        // 对密码进行加密
        String encryptedPassword = Md5Crypt(user.getPassword());
        user.setPassword(encryptedPassword);

        // 进行注册
        int insert = userMapper.insert(user);
        return insert > 0;
    }


    private String Md5Crypt(String password) {
        return DigestUtils.md5Hex(password);
    }

}




