package hope.smarteditor.document.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hope.smarteditor.common.model.entity.FavoriteTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hope.smarteditor.common.model.entity.TemplateDocument;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author LoveF
* @description 针对表【favorite_template】的数据库操作Mapper
* @createDate 2024-05-25 15:34:44
* @Entity hope.smarteditor.common.model.entity.FavoriteTemplate
*/
@Mapper
public interface FavoriteTemplateMapper extends BaseMapper<FavoriteTemplate> {
}




