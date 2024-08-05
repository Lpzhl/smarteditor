package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.api.UserDubboService;
import hope.smarteditor.common.model.entity.*;
import hope.smarteditor.document.mapper.*;
import hope.smarteditor.document.service.TemplateDocumentService;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
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

    @DubboReference(version = "1.0.0", group = "user", check = false)
    private UserDubboService userDubboService;

    @Autowired
    private DocumentOperationMapper documentOperationMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;
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

        //判断是否还具有模板的使用次数

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


        String name = userDubboService.getUserNameByUserId(userId);
        Documentpermissions documentpermissions = new Documentpermissions();
        documentpermissions.setDocumentId(document.getId());
        documentpermissions.setUserId(document.getUserId());
        documentpermissions.setPermissionId(1L); // 设置为创建者，权限为可编辑
        documentpermissionsMapper.insert(documentpermissions);

        DocumentOperation documentOperation = new DocumentOperation();
        documentOperation.setDocumentId(document.getId());
        documentOperation.setUserId(userId);
        documentOperation.setOperation("创建");
        documentOperation.setDescription("用户 " + name + " 创建了文档《 " + documentMapper.selectById(document.getId()).getName()+ "》");
        documentOperation.setOperationTime(new Date());
        documentOperationMapper.insert(documentOperation);

        //版本信息
        // 保存旧版本到文档版本表
        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocumentId(document.getId());
        documentVersion.setVersion(getNextVersionNumber(document.getId()));
        documentVersion.setContent(document.getContent());
        documentVersion.setSummary(document.getSummary());

        documentVersion.setUsername(userDubboService.getUserNameByUserId(userId));
        documentVersion.setUpdateTime(new Date());
        documentVersionMapper.insert(documentVersion);

        return document.getId();
    }


    private double getNextVersionNumber(Long documentId) {
        // 使用QueryWrapper获取文档版本表中该文档的最大版本号
        QueryWrapper<DocumentVersion> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId)
                .orderByDesc("version")
                .last("LIMIT 1");

        DocumentVersion documentVersion = documentVersionMapper.selectOne(wrapper);
        Double maxVersion = null;

        //如果documentVersion 为null 则版本号为1.00，否则版本号加0.01
        if (documentVersion != null) {
            maxVersion = documentVersion.getVersion();
        }

        // 如果文档版本表中没有记录，则版本号为1.00，否则版本号加0.01
        return (maxVersion == null) ? 1.00 : Math.round((maxVersion + 0.01) * 100.0) / 100.0;
    }
}




