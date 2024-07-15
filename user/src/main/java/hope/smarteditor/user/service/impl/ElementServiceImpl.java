package hope.smarteditor.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.ElementDTO;
import hope.smarteditor.common.model.entity.Element;
import hope.smarteditor.common.model.entity.User;
import hope.smarteditor.common.model.vo.ElementVO;
import hope.smarteditor.user.mapper.ElementMapper;
import hope.smarteditor.user.mapper.UserMapper;
import hope.smarteditor.user.service.ElementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LoveF
 * @description 针对表【element】的数据库操作Service实现
 * @createDate 2024-07-11 12:31:48
 */
@Service
public class ElementServiceImpl extends ServiceImpl<ElementMapper, Element>
        implements ElementService {

    @Autowired
    private ElementMapper elementMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ElementVO> getIndexElement() {
        QueryWrapper<Element> queryWrapper = new QueryWrapper<>();
        queryWrapper.last("limit 10");
        List<Element> elements = elementMapper.selectList(queryWrapper);
        List<ElementVO> elementVOs = new ArrayList<>();

        for (Element element : elements) {
            ElementVO elementVO = new ElementVO();
            BeanUtils.copyProperties(element, elementVO);

            // 根据userId查询用户昵称
            if (element.getUserId() != null) {
                User user = userMapper.selectById(element.getUserId());
                if (user != null) {
                    elementVO.setNickname(user.getNickname());
                } else {
                    // 如果找不到对应的用户，设置为系统推荐
                    elementVO.setNickname("系统推荐");
                }
            } else {
                // 如果userId为null，设置为系统推荐
                elementVO.setNickname("系统推荐");
            }

            elementVOs.add(elementVO);
        }

        return elementVOs;
    }

    @Override
    public Object deleteElement(String id) {
        return elementMapper.deleteById(id);
    }

    @Override
    public List<Element> getUserElement(Long userId) {
        Element element = new Element();
        QueryWrapper<Element> elementQueryWrapper = new QueryWrapper<>();
        elementQueryWrapper.eq("user_id", userId);
        return elementMapper.selectList(elementQueryWrapper);
    }

    @Override
    public String uploadElement(ElementDTO elementDTO) {
        Element element = new Element();
        BeanUtils.copyProperties(elementDTO, element);
        elementMapper.insert(element);
        return "上传成功";
    }

    @Override
    public String editElement(ElementDTO elementDTO) {
        Element element = new Element();
        BeanUtils.copyProperties(elementDTO, element);
        elementMapper.updateById(element);
        return "修改成功";
    }

    @Override
    public Object addElement(Long id, Long userId) {
        Element element = new Element();
        Element element1 = elementMapper.selectById(id);
        BeanUtils.copyProperties(element1, element);
        element.setId(null);
        element.setUserId(userId);
        return elementMapper.insert(element);
    }
}




