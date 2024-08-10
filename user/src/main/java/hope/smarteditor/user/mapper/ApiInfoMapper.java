package hope.smarteditor.user.mapper;

import hope.smarteditor.common.model.entity.ApiInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author LoveF
* @description 针对表【api_info】的数据库操作Mapper
* @createDate 2024-08-09 00:37:37
* @Entity hope.smarteditor.common.model.entity.ApiInfo
*/
public interface ApiInfoMapper extends BaseMapper<ApiInfo> {

    @Select("select * from api_info where name = #{aiName}")
    ApiInfo findByName(String aiName);
}




