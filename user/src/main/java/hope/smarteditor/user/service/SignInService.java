package hope.smarteditor.user.service;

import hope.smarteditor.common.model.entity.SignIn;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author LoveF
* @description 针对表【sign_in】的数据库操作Service
* @createDate 2024-07-06 16:54:23
*/
public interface SignInService extends IService<SignIn> {

    String sign(Long userId);

    boolean checkSign(Long userId);
}
