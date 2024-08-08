package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.Documentpermissions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
* @author LoveF
* @description 针对表【documentpermissions】的数据库操作Mapper
* @createDate 2024-05-28 08:57:20
* @Entity hope.smarteditor.common.model.entity.Documentpermissions
*/
@Mapper
public interface DocumentpermissionsMapper extends BaseMapper<Documentpermissions> {
    @Select("select * from documentpermissions where document_id = #{documentId} and user_id = #{userId}")
    Documentpermissions selectByPrimaryKey(Long documentId, Long userId);
}




