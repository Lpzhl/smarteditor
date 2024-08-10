package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.Styles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author LoveF
* @description 针对表【styles(存储文档样式的基本信息)】的数据库操作Service
* @createDate 2024-08-10 22:13:42
*/
public interface StylesService extends IService<Styles> {

    List<Styles> getStylesByUserId(int userId);
}
