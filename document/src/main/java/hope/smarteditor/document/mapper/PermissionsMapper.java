package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.Permissions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LoveF
* @description 针对表【permissions】的数据库操作Mapper
* @createDate 2024-05-28 20:33:38
* @Entity hope.smarteditor.common.model.entity.Permissions
*/
@Mapper
public interface PermissionsMapper extends BaseMapper<Permissions> {

}




