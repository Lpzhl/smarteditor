package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.DeletedInfo;
import hope.smarteditor.document.service.DeletedInfoService;
import hope.smarteditor.document.mapper.DeletedInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【deleted_info】的数据库操作Service实现
* @createDate 2024-08-06 20:15:20
*/
@Service
public class DeletedInfoServiceImpl extends ServiceImpl<DeletedInfoMapper, DeletedInfo>
    implements DeletedInfoService{

}




