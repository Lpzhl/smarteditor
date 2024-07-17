package hope.smarteditor.user.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hope.smarteditor.api.DocumentDubboService;
import hope.smarteditor.common.model.dto.UserLoginDTO;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.BaiduResultVO;
import hope.smarteditor.common.model.vo.OcrVO;
import hope.smarteditor.user.service.UserService;
import hope.smarteditor.user.mapper.UserMapper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



/**
* @author LoveF
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-05-13 20:51:50
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    public  static final String BASE_URL = "https://f504iccf72ke3bha.aistudio-hub.baidu.com";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucketName;

    @DubboReference(version = "1.0.0", group = "document", check = false)
    private DocumentDubboService documentDubboService;
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
        user.setAvatar("https://cdn.jsdelivr.net/gh/xlc520/MyImage/img/20210501132958.png");
        user.setNickname("新用户");

        // 进行注册
        int insert = userMapper.insert(user);

        documentDubboService.createFolder("默认文件夹",user.getId());
        return insert > 0;
    }

    @Override
    public OcrVO ocr(MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, IOException, MinioException {
        // 1.首先把用户选择的图片上传到minio并且获取返回的url
        String objectName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        System.out.println("objectName = " + objectName);
        InputStream inputStream = file.getInputStream();

        // 检查存储桶是否存在，不存在则创建
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            //设置存储桶的访问权限
        }
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        String fullUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        URL url = new URL(fullUrl);
        String baseUrl = url.getProtocol() + "://" + url.getHost() + ":9000" + url.getPath();

        // 2.调用python 的ocr接口
        String url1 = BASE_URL+"/img";
        JSONObject json = new JSONObject();
        json.put("image_path", baseUrl);

        HttpRequest request = HttpRequest.post(url1)
                .form("image_path", baseUrl)  // 使用form方法设置form-data
                .header("Content-Type", "multipart/form-data");  // 设置请求头为form-data类型
        HttpResponse response = request.execute();
        String body = response.body();

        // 使用Gson解析JSON响应
        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        String message = jsonObject.get("message").getAsString();
        String text = jsonObject.get("text").getAsString();
        String imageBase64 = jsonObject.get("image_base64").getAsString();

        // 打印结果
        System.out.println("Message: " + message);
        System.out.println("Text: " + text);

        // 3.将提取的ocr图片再次上传到minio返回url给前端
        // 将Base64编码的图片解码为字节数组
        byte[] imageBytes = java.util.Base64.getDecoder().decode(imageBase64);
        InputStream imageInputStream = new ByteArrayInputStream(imageBytes);
        String ocrObjectName = UUID.randomUUID().toString() + "-ocr.png";
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(ocrObjectName)
                        .stream(imageInputStream, imageBytes.length, -1)
                        .contentType("image/png")
                        .build()
        );
        String ocrFullUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(ocrObjectName)
                        .build()
        );

        // 返回结果
        OcrVO ocrVO = new OcrVO();
        ocrVO.setOcrImage(ocrFullUrl);
        ocrVO.setText(text);
        ocrVO.setMessage(message);
        return ocrVO;
    }

    @Override
    public String textCorrection(String text) {
        String url = BASE_URL +"/textErrorCorrection";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String titleGeneration(String text) {
        String url = BASE_URL +"/textTitleExtraction";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String textSummarization(String text) {
        String url = BASE_URL +"/textSummaries";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);

            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String textContinuation(String text,String passage) {

        String url = BASE_URL +"/textContinuation";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .form("passage", passage)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String paperContentGeneration(String text, String project, String paperType, String directoryType) {
        String url = BASE_URL +"/paperWriting";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .form("project", project)
                    .form("paper_type", paperType)
                    .form("directory_type", directoryType)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String paperOutlineGeneration(String text, String project, String paperType, String directoryType) {
        String url = BASE_URL +"/paperOutline";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .form("project", project)
                    .form("paper_type", paperType)
                    .form("directory_type", directoryType)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public  List<BaiduResultVO> baidu(String text) {
        String url = BASE_URL +"/baidu";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonArray jsonArray = JsonParser.parseString(body).getAsJsonArray();

            List<BaiduResultVO> results = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                BaiduResultVO result = new BaiduResultVO();
                result.setDesc(jsonObject.get("desc").getAsString());
                result.setHref(jsonObject.get("href").getAsString());
                result.setKw(jsonObject.get("kw").getAsString());
                result.setPage(jsonObject.get("page").getAsInt());
                result.setRealUrl(jsonObject.get("real_url").getAsString());
                result.setSite(jsonObject.get("site").getAsString());
                result.setTitle(jsonObject.get("title").getAsString());
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String ocrTable(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 1.首先把用户选择的图片上传到minio并且获取返回的url
        String objectName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        System.out.println("objectName = " + objectName);
        InputStream inputStream = file.getInputStream();

        // 检查存储桶是否存在，不存在则创建
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            //设置存储桶的访问权限
        }
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        String fullUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        URL url = new URL(fullUrl);
        String baseUrl = url.getProtocol() + "://" + url.getHost() + ":9000" + url.getPath();

        // 2.调用python 的ocr接口
        String url1 = BASE_URL +"/tableImg";
        JSONObject json = new JSONObject();

        HttpRequest request = HttpRequest.post(url1)
                .form("image_path", baseUrl)  // 使用form方法设置form-data
                .header("Content-Type", "multipart/form-data");  // 设置请求头为form-data类型
        HttpResponse response = request.execute();
        String body = response.body();

        // 使用Gson解析JSON响应
        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        String message = jsonObject.get("message").getAsString();
        String text = jsonObject.get("text").getAsString();
        String imageBase64 = jsonObject.get("image_base64").getAsString();

        // 打印结果
        System.out.println("Message: " + message);
        System.out.println("Text: " + text);

        return message;
    }

    @Override
    public String asr(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 1.首先把用户选择的图片上传到minio并且获取返回的url
        String objectName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        System.out.println("objectName = " + objectName);
        InputStream inputStream = file.getInputStream();

        // 检查存储桶是否存在，不存在则创建
        boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            //设置存储桶的访问权限
        }
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        String fullUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        URL url = new URL(fullUrl);
        String baseUrl = url.getProtocol() + "://" + url.getHost() + ":9000" + url.getPath();

        // 2.调用python 的ocr接口
        String url1 = BASE_URL +"/asr";
        HttpRequest request = HttpRequest.post(url1)
                .form("image_path", baseUrl)  // 使用form方法设置form-data
                .header("Content-Type", "multipart/form-data");  // 设置请求头为form-data类型
        HttpResponse response = request.execute();
        String body = response.body();

        // 使用Gson解析JSON响应
        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
        String message = jsonObject.get("message").getAsString();
        String text = jsonObject.get("text").getAsString();
        String imageBase64 = jsonObject.get("image_base64").getAsString();

        // 打印结果
        System.out.println("Message: " + message);
        System.out.println("Text: " + text);

        return message;
    }

    @Override
    public String createChart(String text) {
        String url = BASE_URL +"/createChart";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String format(String text) {
        String url = BASE_URL + "/fixFormat";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String rewrite(String text) {
        String url = BASE_URL + "/rewrite";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String expansion(String text) {
        String url = BASE_URL + "/expansion";
        try {
            HttpResponse response = HttpRequest.post(url)
                     .form("text", text)
                     .execute();
                String body = response.body();
                JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
                String answer = jsonObject.get("answer").getAsString();

                System.out.println(answer);
                return answer;
            } catch (Exception e) {
                System.err.println("请求失败：" + e.getMessage());
                e.printStackTrace();
                return null;
            }
    }

    @Override
    public String abbreviation(String text) {
        String url = BASE_URL + "/abbreviation";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();

            System.out.println(answer);
            return answer;
        } catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String polish(String text,String requirement) {

        String url = BASE_URL + "/polish";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                     .form("requirement", requirement)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();
            return answer;
    }catch (Exception e) {
                System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String dataVisualization(String text, String imageType) {
        String url = BASE_URL + "/dataVisualization";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .form("imageType", imageType)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();
            return answer;
        }catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public String translate(String text) {
        String url = BASE_URL + "/textTranslation";
        try {
            HttpResponse response = HttpRequest.post(url)
                    .form("text", text)
                    .execute();
            String body = response.body();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            String answer = jsonObject.get("answer").getAsString();
            return answer;
        }catch (Exception e) {
            System.err.println("请求失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String Md5Crypt(String password) {
        return DigestUtils.md5Hex(password);
    }

}




