package hope.smarteditor.user.service;

import hope.smarteditor.common.model.dto.ApiInfoDTO;
import hope.smarteditor.common.model.entity.ApiInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Select;

/**
* @author LoveF
* @description 针对表【api_info】的数据库操作Service
* @createDate 2024-08-09 00:37:37
*/
public interface ApiInfoService extends IService<ApiInfo> {
    ApiInfo findByName(String aiName);

    ApiInfo updateApiInfo(ApiInfoDTO apiInfoDTO);

}
