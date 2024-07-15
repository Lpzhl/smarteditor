package hope.smarteditor.document.mapper;

import hope.smarteditor.common.model.entity.DocumentFolder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author LoveF
* @description 针对表【document_folder】的数据库操作Mapper
* @createDate 2024-06-08 17:44:22
* @Entity hope.smarteditor.common.model.entity.DocumentFolder
*/
@Mapper
public interface DocumentFolderMapper extends BaseMapper<DocumentFolder> {

    @Select("SELECT document_id FROM document_folder WHERE folder_id = #{folderId}")
    List<Long> getDocumentIdsByFolderId(Long folderId);
}




