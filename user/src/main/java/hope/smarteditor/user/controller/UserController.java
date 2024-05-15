package hope.smarteditor.user.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.constant.JwtClaimsConstant;
import hope.smarteditor.common.constant.MessageConstant;
import hope.smarteditor.common.model.dto.UserLoginDTO;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.UserLoginVO;
import hope.smarteditor.common.model.vo.UserVO;
import hope.smarteditor.common.properties.JwtProperties;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.common.utils.JwtUtil;
import hope.smarteditor.user.annotation.LzhLog;
import hope.smarteditor.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * (User)表控制层
 *
 * @author makejava
 * @since 2024-05-13 20:48:28
 */
@RestController
@RequestMapping("user")
@Slf4j
@Api(tags = "用户管理接口")
public class UserController {
    /**
     * 服务对象
     */
    @Resource
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;



    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    @LzhLog
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        User user = userService.login(userLoginDTO);
        System.out.println("user = " + user);
        if(user==null){
            return Result.error(ErrorCode.PASSWORD_ERROR.getMessage());
        }
        //用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(String.valueOf(user.getId()))
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param user 查询实体
     * @return 所有数据
     */
    @GetMapping
    @ApiOperation("分页查询所有数据")
    @LzhLog
    public Result<IPage<UserVO>> selectAll(Page<User> page, UserVO userVO) {
        User user = new User();

        BeanUtils.copyProperties(userVO,user);

        Page<User> userPage = userService.page(page, new QueryWrapper<>(user));

        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());

        // 创建一个新的分页对象，将查询结果封装到其中
        Page<UserVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        resultPage.setRecords(userVOList);

        return Result.success(resultPage);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param userid 主键
     * @return 单条数据
     */
    @GetMapping("{userid}")
    @ApiOperation("通过主键查询单条数据")
    @LzhLog
    public Result<UserVO> selectOne(@PathVariable Serializable userid) {
        User user = userService.getById(userid);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return Result.success(userVO);
    }

    /**
     * 新增数据
     *
     * @param user 实体对象
     * @return 新增结果
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    @LzhLog
    public Result<Boolean> register(@RequestBody User user) {
        if(userService.register(user)){
            return  Result.success(MessageConstant.ACCOUNT_SUCCESSFUL);
        }else {
            return Result.error(MessageConstant.ALREADY_EXISTS);
        }

    }

    /**
     * 修改数据
     *
     * @param user 实体对象
     * @return 修改结果
     */
    @PutMapping("/update")
    @ApiOperation("修改用户信息")
    @LzhLog
    public Result update(@RequestBody User user) {
        if(userService.updateUser(user)){
            return Result.success(null,ErrorCode.SUCCESS.getCode(), MessageConstant.OPERATION_SUCCESSFUL);
        }else{
            return Result.error(String.valueOf(ErrorCode.OPERATION_ERROR.getMessage()),ErrorCode.OPERATION_ERROR.getCode());
        }
    }

    /**
     * 删除数据
     *
     * @param userid 主键结合
     * @return 删除结果
     */
    @DeleteMapping("/delete/{userid}")
    @ApiOperation("删除用户信息")
    @LzhLog
    public Result delete(@PathVariable("userid") Long userid) {
        return Result.success(userService.removeById(userid));
    }


    /**
     * 通过token查询用户数据
     * @param token
     * @return
     */


    @GetMapping("/getUserByToken")
    @ApiOperation("通过token查询用户数据")
    @LzhLog
    public Result<UserVO> getUserByToken( String token) {
        // 解析jwt
        String secretKey = jwtProperties.getSecretKey();
        Claims claims = JwtUtil.parseJWT(secretKey, token);

        // 从claims中获取用户信息
        Long userId = null;
        Pattern pattern = Pattern.compile("userId=(\\d+)");
        Matcher matcher = pattern.matcher(claims.toString());
        if (matcher.find()) {
            userId = Long.valueOf(matcher.group(1));
        }
        log.info("userId:{}",userId);
        // 根据用户id查询用户信息
        User user = userService.getById(userId);

        // 创建UserVO对象
        UserVO userVO = new UserVO();

        BeanUtils.copyProperties(user, userVO);

        // 创建Result对象
        Result<UserVO> result = new Result<>();
        result.setData(userVO);
        result.setMsg(ErrorCode.SUCCESS.getMessage());

        return result;
    }


    // 辅助方法，用于将User对象转换为UserVO对象
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}

