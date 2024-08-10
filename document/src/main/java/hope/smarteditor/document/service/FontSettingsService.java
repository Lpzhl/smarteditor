package hope.smarteditor.document.service;

import hope.smarteditor.common.model.entity.FontSettings;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author LoveF
* @description 针对表【font_settings(存储每个样式元素的字体格式、大小和行间距等设置)】的数据库操作Service
* @createDate 2024-08-10 22:13:42
*/
public interface FontSettingsService extends IService<FontSettings> {

    FontSettings getFontSettingsByElementId(Integer elementId);
}
