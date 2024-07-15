package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.Folder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author LoveF
* @description 针对表【folder】的数据库操作Mapper
* @createDate 2024-06-08 14:02:31
* @Entity hope.smarteditor.common.model.entity.Folder
*/
@Mapper
public interface FolderMapper extends BaseMapper<Folder> {

    @Select("SELECT * FROM folder WHERE name LIKE CONCAT('%', #{keyword}, '%') AND user_id = #{userId}")
    List<Folder> searchFoldersByName(@Param("keyword") String keyword, @Param("userId") Long userId);

}




