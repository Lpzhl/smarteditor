package hope.smarteditor.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hope.smarteditor.common.model.entity.RecentDocuments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author LoveF
 * @description 针对表【recent_documents】的数据库操作Mapper
 * @createDate 2024-07-06 00:49:18
 * @Entity hope.smarteditor.common.model.entity.RecentDocuments
 */
@Mapper

public interface RecentDocumentsMapper extends BaseMapper<RecentDocuments> {

    @Select("SELECT * FROM recent_documents WHERE user_id = #{userId} AND document_id = #{docId}")
    RecentDocuments findByUserIdAndDocumentId(@Param("userId") Long userId, @Param("docId") Long docId);

    @Select("SELECT document_id FROM recent_documents WHERE user_id = #{userId} ORDER BY access_time DESC LIMIT #{limit}")
    List<Long> selectRecentDocumentIdsByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}




