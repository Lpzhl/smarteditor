package hope.smarteditor.document.dubboImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hope.smarteditor.api.DocumentDubboService;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.document.mapper.DocumentMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * author lzh
 */

@Service
@DubboService
public class DocumentDubboServiceImpl implements DocumentDubboService {

    @Resource
    private DocumentMapper documentMapper;
    @Override
    public List<Document> getUserAllDocumentInfo(String userId) {
        QueryWrapper <Document> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return documentMapper.selectList(queryWrapper);
    }
}
