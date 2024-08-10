package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.StyleElements;
import hope.smarteditor.document.service.StyleElementsService;
import hope.smarteditor.document.mapper.StyleElementsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author LoveF
* @description 针对表【style_elements(存储每种样式中各级标题和正文的基本信息)】的数据库操作Service实现
* @createDate 2024-08-10 22:13:42
*/
@Service
public class StyleElementsServiceImpl extends ServiceImpl<StyleElementsMapper, StyleElements>
    implements StyleElementsService{

    @Resource
    private StyleElementsMapper styleElementsMapper;
    @Override
    public List<StyleElements> getElementsByStyleId(Integer styleId) {
        StyleElements styleElements = new StyleElements();
        styleElements.setStyleId(styleId);
        return styleElementsMapper.selectList(new QueryWrapper<>(styleElements));
    }
}




