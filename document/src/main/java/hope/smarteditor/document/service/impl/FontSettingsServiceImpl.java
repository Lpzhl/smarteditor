package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.FontSettings;
import hope.smarteditor.document.service.FontSettingsService;
import hope.smarteditor.document.mapper.FontSettingsMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【font_settings】的数据库操作Service实现
* @createDate 2024-08-11 18:58:25
*/
@Service
public class FontSettingsServiceImpl extends ServiceImpl<FontSettingsMapper, FontSettings>
    implements FontSettingsService{

}




