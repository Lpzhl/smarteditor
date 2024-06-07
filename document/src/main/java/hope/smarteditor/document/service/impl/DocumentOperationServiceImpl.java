package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.DocumentOperation;
import hope.smarteditor.document.service.DocumentOperationService;
import hope.smarteditor.document.mapper.DocumentOperationMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【document_operation】的数据库操作Service实现
* @createDate 2024-06-06 17:38:39
*/
@Service
public class DocumentOperationServiceImpl extends ServiceImpl<DocumentOperationMapper, DocumentOperation>
    implements DocumentOperationService{

}




