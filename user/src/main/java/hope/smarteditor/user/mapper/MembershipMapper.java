package hope.smarteditor.user.mapper;

import hope.smarteditor.common.model.entity.Membership;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
* @author LoveF
* @description 针对表【membership】的数据库操作Mapper
* @createDate 2024-07-10 02:56:13
* @Entity hope.smarteditor.common.model.entity.Membership
*/
@Mapper
public interface MembershipMapper extends BaseMapper<Membership> {

    @Select("SELECT * FROM membership WHERE end_date < CURDATE()")
    List<Membership> findExpiredMemberships();

    @Select("SELECT * FROM membership WHERE user_id = #{userId} ORDER BY end_date DESC LIMIT 1")
    Membership selectByUserId(Long userId);
}




