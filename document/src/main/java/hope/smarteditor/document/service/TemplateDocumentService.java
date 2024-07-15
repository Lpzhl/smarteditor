package hope.smarteditor.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.entity.TemplateDocument;

/**
* @author LoveF
* @description 针对表【template_document】的数据库操作Service
* @createDate 2024-07-12 17:32:34
*/
public interface TemplateDocumentService extends IService<TemplateDocument> {

    void saveTemplate(Long id, Long userId);

    Long useTemplate(Long id, Long userId);

}
