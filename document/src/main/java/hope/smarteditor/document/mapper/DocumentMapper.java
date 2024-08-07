package hope.smarteditor.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hope.smarteditor.common.model.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author LoveF
* @description 针对表【document】的数据库操作Mapper
* @createDate 2024-05-22 14:26:07
* @Entity generator.domain.Document
*/
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {


    @Select("SELECT * FROM document WHERE name LIKE CONCAT('%', #{keyword}, '%') AND user_id = #{userId}")
    List<Document> searchDocumentsByName(@Param("keyword") String keyword, @Param("userId") Long userId);
    @Select("SELECT * FROM document WHERE user_id = #{userId} AND is_deleted = 1")
    List<Document> getDeletedDocuments(@Param("userId") Long userId);

    @Update("UPDATE document SET is_deleted = 0 WHERE id = #{documentId} AND is_deleted = 1 ")
    void recoverDocument(@Param("documentId") Long documentId);
}



