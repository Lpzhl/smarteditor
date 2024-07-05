package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Share;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author LoveF
* @description 针对表【share】的数据库操作Service
* @createDate 2024-07-05 17:20:22
*/
public interface ShareService extends IService<Share> {

    Share shareDocument(Long id, Integer validDays, String editPermission);

    Share shareFolder(Long id, Integer validDays, String editPermission);

    Document handleShareDocument(String link);
}
