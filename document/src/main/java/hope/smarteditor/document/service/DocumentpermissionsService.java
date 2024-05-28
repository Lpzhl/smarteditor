package hope.smarteditor.document.service;

import hope.smarteditor.common.model.dto.DocumentpermissionsDTO;
import hope.smarteditor.common.model.entity.Documentpermissions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author LoveF
* @description 针对表【documentpermissions】的数据库操作Service
* @createDate 2024-05-28 08:57:20
*/
public interface DocumentpermissionsService extends IService<Documentpermissions> {

    boolean setUserAbility(DocumentpermissionsDTO documentpermissionsDTO);

}
