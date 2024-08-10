package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Styles;
import hope.smarteditor.document.service.StylesService;
import hope.smarteditor.document.mapper.StylesMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author LoveF
* @description 针对表【styles(存储文档样式的基本信息)】的数据库操作Service实现
* @createDate 2024-08-10 22:13:42
*/
@Service
public class StylesServiceImpl extends ServiceImpl<StylesMapper, Styles>
    implements StylesService{

    @Resource
    private StylesMapper stylesMapper;

    @Override
    public List<Styles> getStylesByUserId(int userId) {
        Styles styles = new Styles();
        styles.setOwnerId(userId);

        return stylesMapper.selectList(new QueryWrapper<>(styles));
    }
}




