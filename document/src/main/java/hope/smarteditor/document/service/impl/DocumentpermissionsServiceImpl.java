package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.model.dto.DocumentpermissionsDTO;
import hope.smarteditor.common.model.entity.Documentpermissions;
import hope.smarteditor.document.service.DocumentpermissionsService;
import hope.smarteditor.document.mapper.DocumentpermissionsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author LoveF
* @description 针对表【documentpermissions】的数据库操作Service实现
* @createDate 2024-05-28 08:57:20
*/
@Service
public class DocumentpermissionsServiceImpl extends ServiceImpl<DocumentpermissionsMapper, Documentpermissions>
    implements DocumentpermissionsService{

    @Resource
    private DocumentpermissionsMapper documentpermissionsMapper;

    @Override
    public boolean setUserAbility(DocumentpermissionsDTO documentpermissionsDTO) {
        Documentpermissions documentpermissions = new Documentpermissions();

        BeanUtils.copyProperties(documentpermissionsDTO, documentpermissions);

        if(documentpermissionsMapper.insert(documentpermissions)>0)
        {
            return true;
        }else{
           throw new BusinessException(ErrorCode.SET_USER_VISBILITY_ERROR);
        }
    }
}




