package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.DocumentVersion;
import hope.smarteditor.document.service.DocumentVersionService;
import hope.smarteditor.document.mapper.DocumentVersionMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【document_version】的数据库操作Service实现
* @createDate 2024-06-06 17:41:49
*/
@Service
public class DocumentVersionServiceImpl extends ServiceImpl<DocumentVersionMapper, DocumentVersion>
    implements DocumentVersionService{

}




