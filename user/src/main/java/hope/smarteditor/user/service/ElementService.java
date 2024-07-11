package hope.smarteditor.user.service;

import hope.smarteditor.common.model.dto.ElementDTO;
import hope.smarteditor.common.model.entity.Element;
import hope.smarteditor.common.model.vo.ElementVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author LoveF
* @description 针对表【element】的数据库操作Service
* @createDate 2024-07-11 15:05:02
*/
public interface ElementService extends IService<Element> {

    List<ElementVO> getIndexElement();

    Object deleteElement(String id);

    List<Element> getUserElement(Long userId);

    String uploadElement(ElementDTO elementDTO);

    String editElement(ElementDTO elementDTO);

}
