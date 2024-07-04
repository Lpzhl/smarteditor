package hope.smarteditor.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.dto.UserLoginDTO;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.BaiduResultVO;
import hope.smarteditor.common.model.vo.OcrVO;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
* @author LoveF
* @description 针对表【user】的数据库操作Service
* @createDate 2024-05-13 20:51:50
*/
public interface UserService extends IService<User> {

    User login(UserLoginDTO userLoginDTO);

    boolean updateUser(User user);

    boolean register(User user);

    OcrVO ocr(MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException, MinioException;

    String textCorrection(String text);

    String titleGeneration(String text);

    String textSummarization(String text);

    String textContinuation(String text,String passage);

    String paperContentGeneration(String text, String project, String paperType, String directoryType);

    String paperOutlineGeneration(String text, String project, String paperType, String directoryType);

    List<BaiduResultVO> baidu(String text);
}
