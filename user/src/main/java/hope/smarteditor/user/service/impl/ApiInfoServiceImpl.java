package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.dto.ApiInfoDTO;
import hope.smarteditor.common.model.entity.ApiInfo;
import hope.smarteditor.user.service.ApiInfoService;
import hope.smarteditor.user.mapper.ApiInfoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author LoveF
* @description 针对表【api_info】的数据库操作Service实现
* @createDate 2024-08-09 00:37:37
*/
@Service
public class ApiInfoServiceImpl extends ServiceImpl<ApiInfoMapper, ApiInfo>
    implements ApiInfoService{


    @Resource
    private ApiInfoMapper apiInfoMapper;

    @Override
    public ApiInfo findByName(String aiName) {
        return apiInfoMapper.findByName(aiName);
    }

    @Override
    public ApiInfo updateApiInfo(ApiInfoDTO apiInfoDTO) {
        ApiInfo apiInfo = new ApiInfo();
        BeanUtils.copyProperties(apiInfoDTO, apiInfo);
        apiInfoMapper.updateById(apiInfo);
        return apiInfo;
    }
}




