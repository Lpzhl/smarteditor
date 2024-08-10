package hope.smarteditor.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.ApiCalls;
import hope.smarteditor.user.service.ApiCallsService;
import hope.smarteditor.user.mapper.ApiCallsMapper;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【api_calls】的数据库操作Service实现
* @createDate 2024-08-09 00:57:07
*/
@Service
public class ApiCallsServiceImpl extends ServiceImpl<ApiCallsMapper, ApiCalls>
    implements ApiCallsService{

}




