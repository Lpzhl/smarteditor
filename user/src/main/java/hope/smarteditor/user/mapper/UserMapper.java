package hope.smarteditor.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hope.smarteditor.common.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

/**
* @author LoveF
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-05-13 20:51:50
* @Entity generator.domain.User
*/
@Mapper()
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where username = #{username}")
    User findByUsername(String username);
}




