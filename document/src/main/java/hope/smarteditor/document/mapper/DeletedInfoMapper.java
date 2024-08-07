package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.DeletedInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author LoveF
* @description 针对表【deleted_info】的数据库操作Mapper
* @createDate 2024-08-06 20:15:20
* @Entity hope.smarteditor.common.model.entity.DeletedInfo
*/
public interface DeletedInfoMapper extends BaseMapper<DeletedInfo> {

    @Select("SELECT * FROM deleted_info WHERE document_id = #{documentId}")
    DeletedInfo selectO(Long documentId);
}




