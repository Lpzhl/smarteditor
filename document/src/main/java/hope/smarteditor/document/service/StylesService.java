package hope.smarteditor.document.service;

import hope.smarteditor.common.model.dto.StyleDeleteDTO;
import hope.smarteditor.common.model.dto.StyleEditDTO;
import hope.smarteditor.common.model.entity.Styles;
import com.baomidou.mybatisplus.extension.service.IService;
import hope.smarteditor.common.model.vo.StyleVO;

import java.util.List;

/**
* @author LoveF
* @description 针对表【styles(存储文档样式的基本信息)】的数据库操作Service
* @createDate 2024-08-11 18:47:20
*/
public interface StylesService extends IService<Styles> {

    List<StyleVO> getStyleByUserId(Long userId);

    StyleVO editStyle(StyleEditDTO styleEditDTO);

    String deleteStyle(StyleDeleteDTO styleDeleteDTO);


    StyleVO addStyle(StyleEditDTO styleEditDTO);

}
