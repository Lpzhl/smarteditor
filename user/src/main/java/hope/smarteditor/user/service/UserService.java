package hope.smarteditor.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.dto.UserLoginDTO;
import hope.smarteditor.common.model.entity.User;

/**
* @author LoveF
* @description 针对表【user】的数据库操作Service
* @createDate 2024-05-13 20:51:50
*/
public interface UserService extends IService<User> {

    User login(UserLoginDTO userLoginDTO);

    boolean updateUser(User user);

    boolean register(User user);
}
