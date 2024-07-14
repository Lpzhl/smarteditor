package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Docu;
import hope.smarteditor.document.service.DocuService;
import hope.smarteditor.document.mapper.DocuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author LoveF
* @description 针对表【docu】的数据库操作Service实现
* @createDate 2024-07-14 21:29:15
*/
@Service
public class DocuServiceImpl extends ServiceImpl<DocuMapper, Docu>
    implements DocuService{

    @Autowired
    private DocuMapper docuMapper;

    @Override
    public List<String> getProfessionCategories() {
        return docuMapper.getProfessionCategories();
    }

    @Override
    public List<String> getSubjectCategories() {
        return docuMapper.getSubjectCategories();
    }

    @Override
    public List<Docu> getList() {
        QueryWrapper<Docu> docuQueryWrapper = new QueryWrapper<>();
        docuQueryWrapper.last("LIMIT 50");
        return docuMapper.selectList(docuQueryWrapper);
    }

    @Override
    public List<Docu> search(String keyword) {
        QueryWrapper<Docu> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyword)
                .or().like("context", keyword)
                .or().like("profession", keyword)
                .or().like("subject", keyword)
                .or().like("document_key", keyword)
                .last("LIMIT 30");

        return docuMapper.selectList(queryWrapper);
    }

}




