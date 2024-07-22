package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.DocumentOperation;
import hope.smarteditor.document.service.DocumentOperationService;
import hope.smarteditor.document.mapper.DocumentOperationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author LoveF
* @description 针对表【document_operation】的数据库操作Service实现
* @createDate 2024-06-06 17:38:39
*/
@Service
public class DocumentOperationServiceImpl extends ServiceImpl<DocumentOperationMapper, DocumentOperation>
    implements DocumentOperationService{

    @Autowired
    private DocumentOperationMapper baseMapper;
    @Override
    public List<DocumentOperation> getDocumentLog(Long documentId) {
        QueryWrapper<DocumentOperation> documentOperationQueryWrapper = new QueryWrapper<>();
        documentOperationQueryWrapper.eq("document_id",documentId);
        return baseMapper.selectList(documentOperationQueryWrapper);
    }
}




