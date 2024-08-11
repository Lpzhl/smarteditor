package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.FontSettingsDTO;
import hope.smarteditor.common.model.dto.StyleDeleteDTO;
import hope.smarteditor.common.model.dto.StyleEditDTO;
import hope.smarteditor.common.model.entity.FontSettings;
import hope.smarteditor.common.model.entity.Styles;
import hope.smarteditor.common.model.vo.FontSettingsVO;
import hope.smarteditor.common.model.vo.StyleVO;
import hope.smarteditor.document.mapper.FontSettingsMapper;
import hope.smarteditor.document.service.StylesService;
import hope.smarteditor.document.mapper.StylesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author LoveF
* @description 针对表【styles(存储文档样式的基本信息)】的数据库操作Service实现
* @createDate 2024-08-11 18:47:20
*/
@Service
public class StylesServiceImpl extends ServiceImpl<StylesMapper, Styles>
    implements StylesService{

    @Resource
    private StylesMapper stylesMapper;

    @Resource
    private FontSettingsMapper fontSettingsMapper;


    @Override
    public List<StyleVO> getStyleByUserId(Long userId) {
        // 首先找出该用户的所有样式
        List<StyleVO> styleVOS = new ArrayList<>();
        LambdaQueryWrapper<Styles> stylesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        stylesLambdaQueryWrapper.eq(Styles::getUserId, userId);
        List<Styles> stylesList = stylesMapper.selectList(stylesLambdaQueryWrapper);

        // 然后根据样式id 找出对应的字体设置
        for (Styles styles : stylesList) {
            LambdaQueryWrapper<FontSettings> fontSettingsLambdaQueryWrapper = new LambdaQueryWrapper<>();
            fontSettingsLambdaQueryWrapper.eq(FontSettings::getStylesId, styles.getStyleId());
            List<FontSettings> fontSettingsList = fontSettingsMapper.selectList(fontSettingsLambdaQueryWrapper);

            // 转换 FontSettings 列表为 FontSettingsVO 列表
            List<FontSettingsVO> fontSettingsVOS = fontSettingsList.stream().map(fontSettings -> {
                FontSettingsVO fontSettingsVO = new FontSettingsVO();
                fontSettingsVO.setId(fontSettings.getId());
                fontSettingsVO.setStylesId(fontSettings.getStylesId());
                fontSettingsVO.setName(fontSettings.getName());
                fontSettingsVO.setFontFamily(fontSettings.getFontFamily());
                fontSettingsVO.setFontSize(fontSettings.getFontSize());
                fontSettingsVO.setLineHeight(fontSettings.getLineHeight());
                // 其他字段的赋值
                return fontSettingsVO;
            }).collect(Collectors.toList());

            // 创建 StyleVO 对象
            StyleVO styleVO = new StyleVO();
            styleVO.setStyleId(styles.getStyleId());
            styleVO.setStyleName(styles.getStyleName());
            styleVO.setUserId(styles.getUserId());
            styleVO.setFontSettingsVOS(fontSettingsVOS); // 将转换后的 FontSettingsVO 列表加入到 StyleVO

            // 将 StyleVO 对象加入到结果列表
            styleVOS.add(styleVO);
        }

        // 返回包含样式和字体设置的列表
        return styleVOS;
    }

    @Override
    @Transactional
    public StyleVO editStyle(StyleEditDTO styleEditDTO) {
        // 获取样式ID
        Long styleId = Long.valueOf(styleEditDTO.getStyleId());

        // 查找现有的样式记录
        Styles existingStyle = stylesMapper.selectById(styleId);
        if (existingStyle == null) {
            throw new RuntimeException("样式不存在，无法编辑");
        }

        // 更新样式名称
        existingStyle.setStyleName(styleEditDTO.getStyleName());
        stylesMapper.updateById(existingStyle);

        // 如果 fontSettings 为空，则跳过字体设置更新
        List<FontSettingsDTO> fontSettingsDTOList = styleEditDTO.getFontSettings();
        if (fontSettingsDTOList != null && !fontSettingsDTOList.isEmpty()) {
            for (FontSettingsDTO fontSettingsDTO : fontSettingsDTOList) {
                // 根据样式ID和字体设置ID查找现有的字体设置
                LambdaQueryWrapper<FontSettings> fontSettingsLambdaQueryWrapper = new LambdaQueryWrapper<>();
                fontSettingsLambdaQueryWrapper.eq(FontSettings::getStylesId, styleId)
                        .eq(FontSettings::getId, fontSettingsDTO.getId());
                FontSettings existingFontSettings = fontSettingsMapper.selectOne(fontSettingsLambdaQueryWrapper);

                if (existingFontSettings != null) {
                    // 更新现有的字体设置
                    existingFontSettings.setFontFamily(fontSettingsDTO.getFontFamily());
                    existingFontSettings.setFontSize(fontSettingsDTO.getFontSize());
                    existingFontSettings.setLineHeight(fontSettingsDTO.getLineHeight());
                    existingFontSettings.setName(fontSettingsDTO.getName());
                    fontSettingsMapper.updateById(existingFontSettings);
                } else {
                    // 插入新的字体设置
                    FontSettings newFontSettings = new FontSettings();
                    newFontSettings.setStylesId(Long.valueOf(existingStyle.getStyleId()));
                    newFontSettings.setFontFamily(fontSettingsDTO.getFontFamily());
                    newFontSettings.setFontSize(fontSettingsDTO.getFontSize());
                    newFontSettings.setLineHeight(fontSettingsDTO.getLineHeight());
                    newFontSettings.setName(fontSettingsDTO.getName());
                    fontSettingsMapper.insert(newFontSettings);
                }
            }
        }

        // 构建返回的 StyleVO
        StyleVO styleVO = new StyleVO();
        styleVO.setStyleId(existingStyle.getStyleId());
        styleVO.setStyleName(existingStyle.getStyleName());
        styleVO.setUserId(existingStyle.getUserId());

        // 查询更新后的字体设置并转换为 VO
        LambdaQueryWrapper<FontSettings> fontSettingsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fontSettingsLambdaQueryWrapper.eq(FontSettings::getStylesId, styleId);
        List<FontSettings> updatedFontSettingsList = fontSettingsMapper.selectList(fontSettingsLambdaQueryWrapper);
        List<FontSettingsVO> fontSettingsVOList = updatedFontSettingsList.stream().map(fontSettings -> {
            FontSettingsVO fontSettingsVO = new FontSettingsVO();
            fontSettingsVO.setId(fontSettings.getId());
            fontSettingsVO.setStylesId(fontSettings.getStylesId());
            fontSettingsVO.setFontFamily(fontSettings.getFontFamily());
            fontSettingsVO.setFontSize(fontSettings.getFontSize());
            fontSettingsVO.setLineHeight(fontSettings.getLineHeight());
            fontSettingsVO.setName(fontSettings.getName());


            return fontSettingsVO;
        }).collect(Collectors.toList());

        styleVO.setFontSettingsVOS(fontSettingsVOList);

        return styleVO;
    }


    @Override
    public String deleteStyle(StyleDeleteDTO styleDeleteDTO) {
        // 删除样式
        stylesMapper.deleteById(styleDeleteDTO.getId());
        return "删除成功";
    }




    @Override
    @Transactional
    public StyleVO addStyle(StyleEditDTO styleEditDTO) {
        // 添加样式
        Styles styles = new Styles();
        styles.setStyleName(styleEditDTO.getStyleName());
        styles.setUserId(Long.valueOf(styleEditDTO.getUserId()));
        stylesMapper.insert(styles);

        // 获取新增样式的ID
        Long styleId = Long.valueOf(styles.getStyleId());

        // 添加字体设置
        List<FontSettingsDTO> fontSettingsDTOList = styleEditDTO.getFontSettings();
        if (fontSettingsDTOList != null && !fontSettingsDTOList.isEmpty()) {
            for (FontSettingsDTO fontSettingsDTO : fontSettingsDTOList) {
                FontSettings fontSettings = new FontSettings();
                fontSettings.setStylesId(styleId);
                fontSettings.setFontFamily(fontSettingsDTO.getFontFamily());
                fontSettings.setFontSize(fontSettingsDTO.getFontSize());
                fontSettings.setLineHeight(fontSettingsDTO.getLineHeight());
                fontSettings.setName(fontSettingsDTO.getName());
                fontSettingsMapper.insert(fontSettings);
            }
        }

        // 构建返回的 StyleVO
        StyleVO styleVO = new StyleVO();
        styleVO.setStyleId(Math.toIntExact(styleId));
        styleVO.setStyleName(styleEditDTO.getStyleName());
        styleVO.setUserId(Long.valueOf(styleEditDTO.getUserId()));

        // 查询更新后的字体设置并转换为 VO
        LambdaQueryWrapper<FontSettings> fontSettingsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fontSettingsLambdaQueryWrapper.eq(FontSettings::getStylesId, styleId);
        List<FontSettings> updatedFontSettingsList = fontSettingsMapper.selectList(fontSettingsLambdaQueryWrapper);
        List<FontSettingsVO> fontSettingsVOList = updatedFontSettingsList.stream().map(fontSettings -> {
            FontSettingsVO fontSettingsVO = new FontSettingsVO();
            fontSettingsVO.setId(fontSettings.getId());
            fontSettingsVO.setFontFamily(fontSettings.getFontFamily());
            fontSettingsVO.setFontSize(fontSettings.getFontSize());
            fontSettingsVO.setLineHeight(fontSettings.getLineHeight());
            fontSettingsVO.setStylesId(fontSettings.getStylesId());
            fontSettingsVO.setName(fontSettings.getName());
            return fontSettingsVO;
        }).collect(Collectors.toList());

        styleVO.setFontSettingsVOS(fontSettingsVOList);
        return styleVO;
    }


}




