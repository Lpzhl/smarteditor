package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.DocumentOperation;
import hope.smarteditor.common.model.entity.DocumentVersion;
import hope.smarteditor.common.model.vo.DocumentVersionVO;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.mapper.DocumentOperationMapper;
import hope.smarteditor.document.service.DocumentVersionService;
import hope.smarteditor.document.mapper.DocumentVersionMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author LoveF
* @description 针对表【document_version】的数据库操作Service实现
* @createDate 2024-06-06 17:41:49
*/
@Service
public class DocumentVersionServiceImpl extends ServiceImpl<DocumentVersionMapper, DocumentVersion>
    implements DocumentVersionService{

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentOperationMapper documentOperationMapper;

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    @Override
    public List<DocumentVersionVO> getDocumentVersion(Long documentId) {
        QueryWrapper<DocumentVersion> documentVersionQueryWrapper = new QueryWrapper<>();
        documentVersionQueryWrapper.eq("document_id", documentId).orderByDesc("version");
        List<DocumentVersion> documentVersions = documentVersionMapper.selectList(documentVersionQueryWrapper);

        List<DocumentVersionVO> documentVersionVOs = documentVersions.stream()
                .map(documentVersion -> {
                    DocumentVersionVO documentVersionVO = new DocumentVersionVO();
                    documentVersionVO.setId(documentVersion.getId());
                    documentVersionVO.setDocumentId(documentVersion.getDocumentId());
                    documentVersionVO.setContent(documentVersion.getContent());
                    documentVersionVO.setUsername(documentVersion.getUsername());
                    documentVersionVO.setVersion(documentVersion.getVersion());
                    documentVersionVO.setUpdateTime(documentVersion.getUpdateTime());
                    documentVersionVO.setSummary(documentVersion.getSummary());
                    return documentVersionVO;
                })
                .collect(Collectors.toList());

        return documentVersionVOs;
    }

    @Override
    public void rollbackDocumentVersion(Long documentId, Long versionId, Long userId) {
        QueryWrapper<DocumentVersion> documentVersionQueryWrapper = new QueryWrapper<>();
        documentVersionQueryWrapper.eq("id", versionId);
        DocumentVersion documentVersion = documentVersionMapper.selectOne(documentVersionQueryWrapper);

        QueryWrapper<Document> documentQueryWrapper = new QueryWrapper<>();
        documentQueryWrapper.eq("id", documentId);
        Document document = documentMapper.selectOne(documentQueryWrapper);
        document.setContent(documentVersion.getContent());
        documentMapper.updateById(document);

        String name = userDubboService.getUserNameByUserId(userId);

        // 操作记录以及版本
        DocumentOperation documentOperation = new DocumentOperation();
        documentOperation.setUserId(userId);
        documentOperation.setDocumentId(documentId);
        documentOperation.setDescription("用户 "+name+"回滚到版本" + documentVersion.getVersion());
        documentOperation.setOperation("回退");
    }

}




