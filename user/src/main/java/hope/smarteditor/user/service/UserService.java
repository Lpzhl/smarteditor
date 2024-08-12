package hope.smarteditor.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.dto.RegisterDTO;
import hope.smarteditor.common.model.dto.UserLoginDTO;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.BaiduResultVO;
import hope.smarteditor.common.model.vo.OcrVO;
import hope.smarteditor.common.model.vo.UserStatisticsVO;
import hope.smarteditor.common.model.vo.UserVO;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
* @author LoveF
* @description 针对表【user】的数据库操作Service
* @createDate 2024-05-13 20:51:50
*/

@Service
public interface UserService extends IService<User> {

    User login(UserLoginDTO userLoginDTO);

    boolean updateUser(User user);

    boolean register(RegisterDTO registerDTO);

    OcrVO ocr(MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException, MinioException;

    String textCorrection(String text);

    String titleGeneration(String text);

    String textSummarization(String text);

    String textContinuation(String text,String passage);

    String paperContentGeneration(String text, String project, String paperType, String directoryType);

    String paperOutlineGeneration(String text, String project, String paperType, String directoryType);

    List<BaiduResultVO> baidu(String text);

    OcrVO ocrTable(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    OcrVO asr(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    String createChart(String text);

    String format(String text);

    String rewrite(String text);

    String expansion(String text);

    String abbreviation(String text);

    String polish(String text,String requirement);

    String dataVisualization(String text, String imageType);

    User findByUsername(String username);

    String translate(String text);

    User authenticate(String username, String password);
    List<Map<String, String>> fetchToutiaoHot();
    String mindMap(String text);

    String paperReview(String text);

    String aiDocumentAssistant(String text, String documentUrl);

    UserStatisticsVO getUsersCount();

    void updateUserStatus(Long userId);

    List<UserVO> getUserList();
}
