package hope.smarteditor.user.mapper;

import hope.smarteditor.common.model.entity.Pricing;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author LoveF
* @description 针对表【Pricing】的数据库操作Mapper
* @createDate 2024-08-10 19:12:54
* @Entity hope.smarteditor.common.model.entity.Pricing
*/
public interface PricingMapper extends BaseMapper<Pricing> {

    @Select("select * from Pricing where is_deleted = 1")
    List<Pricing> selectDeletedList(Object o);
}




