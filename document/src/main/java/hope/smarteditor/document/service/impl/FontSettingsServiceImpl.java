package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.FontSettings;
import hope.smarteditor.document.service.FontSettingsService;
import hope.smarteditor.document.mapper.FontSettingsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author LoveF
* @description 针对表【font_settings(存储每个样式元素的字体格式、大小和行间距等设置)】的数据库操作Service实现
* @createDate 2024-08-10 22:13:42
*/
@Service
public class FontSettingsServiceImpl extends ServiceImpl<FontSettingsMapper, FontSettings>
    implements FontSettingsService{

    @Resource
    private FontSettingsMapper fontSettingsMapper;

    @Override
    public FontSettings getFontSettingsByElementId(Integer elementId) {
        return fontSettingsMapper.getFontSettingsByElementId(elementId);
    }
}




