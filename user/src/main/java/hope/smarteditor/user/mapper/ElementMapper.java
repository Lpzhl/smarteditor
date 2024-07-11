package hope.smarteditor.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hope.smarteditor.common.model.entity.Element;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LoveF
* @description 针对表【element】的数据库操作Mapper
* @createDate 2024-07-11 15:05:02
* @Entity hope.smarteditor.user.domain.Element
*/
@Mapper
public interface ElementMapper extends BaseMapper<Element> {

}




