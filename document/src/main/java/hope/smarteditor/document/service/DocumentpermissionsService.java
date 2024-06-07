package hope.smarteditor.document.service;

import hope.smarteditor.common.model.dto.DocumentPermissionsDTO;
import hope.smarteditor.common.model.entity.Documentpermissions;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.entity.Permissions;

import java.util.List;

/**
* @author LoveF
* @description 针对表【documentpermissions】的数据库操作Service
* @createDate 2024-05-28 08:57:20
*/
public interface DocumentpermissionsService extends IService<Documentpermissions> {

    boolean setUserAbility(DocumentPermissionsDTO documentpermissionsDTO);

    List<Permissions> getPermissionsForUserAndDocument(Long userId, Long documentId);
}
