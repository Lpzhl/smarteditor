package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.Docu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author LoveF
* @description 针对表【docu】的数据库操作Service
* @createDate 2024-07-14 21:29:15
*/
public interface DocuService extends IService<Docu> {
    List<String> getProfessionCategories();
    List<String> getSubjectCategories();

    List<Docu> getList();

    List<Docu> search(String keyword);
}
