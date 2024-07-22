package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Documentpermissions;
import hope.smarteditor.common.model.entity.TemplateDocument;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.mapper.DocumentpermissionsMapper;
import hope.smarteditor.document.mapper.TemplateDocumentMapper;
import hope.smarteditor.document.service.TemplateDocumentService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author LoveF
* @description 针对表【template_document】的数据库操作Service实现
* @createDate 2024-07-12 17:32:34
*/
@Service
public class TemplateDocumentServiceImpl extends ServiceImpl<TemplateDocumentMapper, TemplateDocument>
    implements TemplateDocumentService{

    @Autowired
    private TemplateDocumentMapper templateDocumentMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentpermissionsMapper documentpermissionsMapper;

    @Override
    public void saveTemplate(Long id, Long userId) {
        TemplateDocument templateDocument = new TemplateDocument();
        QueryWrapper<TemplateDocument> templateDocumentQueryWrapper = new QueryWrapper<>();
        templateDocumentQueryWrapper.eq("id", id);
        templateDocument = templateDocumentMapper.selectOne(templateDocumentQueryWrapper);
        TemplateDocument templateDocument1 = new TemplateDocument();
        BeanUtils.copyProperties(templateDocument, templateDocument1);

        templateDocument1.setUserId(userId);
        templateDocumentMapper.insert(templateDocument1);

    }

    @Override
    public Long useTemplate(Long id, Long userId) {
        Document document = new Document();
        QueryWrapper<TemplateDocument> documentQueryWrapper = new QueryWrapper<>();
        documentQueryWrapper.eq("id", id);
        TemplateDocument templateDocument = templateDocumentMapper.selectOne(documentQueryWrapper);
        BeanUtils.copyProperties(templateDocument, document);
        document.setUpdateTime(new Date());
        document.setCreateTime(new Date());
        document.setUserId(userId);
        document.setId(null);
        documentMapper.insert(document);


        Documentpermissions documentpermissions = new Documentpermissions();
        documentpermissions.setDocumentId(document.getId());
        documentpermissions.setUserId(document.getUserId());
        documentpermissions.setPermissionId(1L); // 设置为创建者，权限为可编辑
        documentpermissionsMapper.insert(documentpermissions);

        return document.getId();
    }
}




