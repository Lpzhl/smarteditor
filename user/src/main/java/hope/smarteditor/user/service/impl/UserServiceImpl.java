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

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucketName;


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
        String url1 = "http://192.168.50.150:5000/img";
        JSONObject json = new JSONObject();
        json.put("image_path", baseUrl);

        HttpResponse response = HttpRequest.post(url1)
                .body(json.toString())  // 设置请求体为JSON字符串
                .header("Content-Type", "application/json")  // 设置请求头为JSON类型
                .execute();
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
        String url = "http://192.168.50.150:5000/textErrorCorrection";
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
        String url = "http://192.168.50.150:5000/textTitleExtraction";
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
        String url = "http://192.168.50.150:5000/textSummaries";
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

        String url = "http://192.168.50.150:5000/textContinuation";
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
        String url = "http://192.168.50.150:5000/paperWriting";
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
        String url = "http://192.168.50.150:5000/paperOutline";
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
        String url = "http://192.168.50.150:5000/baidu";
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


    private String Md5Crypt(String password) {
        return DigestUtils.md5Hex(password);
    }

}




