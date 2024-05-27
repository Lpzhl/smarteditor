package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.FavoriteDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author LoveF
* @description 针对表【favorite_document】的数据库操作Mapper
* @createDate 2024-05-25 10:50:47
* @Entity hope.smarteditor.common.model.entity.FavoriteDocument
*/
@Mapper
public interface FavoriteDocumentMapper extends BaseMapper<FavoriteDocument> {

}




