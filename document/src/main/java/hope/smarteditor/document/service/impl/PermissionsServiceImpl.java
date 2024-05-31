package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Permissions;
import hope.smarteditor.document.service.PermissionsService;
import hope.smarteditor.document.mapper.PermissionsMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【permissions】的数据库操作Service实现
* @createDate 2024-05-28 20:33:38
*/
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions>
    implements PermissionsService{

}




