package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.StyleElements;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author LoveF
* @description 针对表【style_elements(存储每种样式中各级标题和正文的基本信息)】的数据库操作Service
* @createDate 2024-08-10 22:13:42
*/
public interface StyleElementsService extends IService<StyleElements> {

    List<StyleElements> getElementsByStyleId(Integer styleId);

}
