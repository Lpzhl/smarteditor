package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.Docu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author LoveF
* @description 针对表【docu】的数据库操作Mapper
* @createDate 2024-07-14 21:29:15
* @Entity hope.smarteditor.common.model.entity.Docu
*/
public interface DocuMapper extends BaseMapper<Docu> {
    @Select("SELECT DISTINCT profession FROM docu WHERE profession IS NOT NULL")
    List<String> getProfessionCategories();

    @Select("SELECT DISTINCT subject FROM docu WHERE subject IS NOT NULL")
    List<String> getSubjectCategories();
}




