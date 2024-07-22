package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.model.dto.DocumentPermissionsDTO;
import hope.smarteditor.common.model.entity.Documentpermissions;
import hope.smarteditor.common.model.entity.Permissions;
import hope.smarteditor.document.mapper.PermissionsMapper;
import hope.smarteditor.document.service.DocumentpermissionsService;
import hope.smarteditor.document.mapper.DocumentpermissionsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author LoveF
* @description 针对表【documentpermissions】的数据库操作Service实现
* @createDate 2024-05-28 08:57:20
*/
@Service
public class DocumentpermissionsServiceImpl extends ServiceImpl<DocumentpermissionsMapper, Documentpermissions>
    implements DocumentpermissionsService{

    @Autowired
    private DocumentpermissionsMapper documentpermissionsMapper;
    @Autowired
    private PermissionsMapper permissionsMapper;

    @Override
    public boolean setUserAbility(DocumentPermissionsDTO documentpermissionsDTO) {
        Documentpermissions documentpermissions = new Documentpermissions();

        BeanUtils.copyProperties(documentpermissionsDTO, documentpermissions);

        if(documentpermissionsMapper.insert(documentpermissions)>0)
        {
            return true;
        }else{
           throw new BusinessException(ErrorCode.SET_USER_VISBILITY_ERROR);
        }
    }

    @Override
    public List<Permissions> getPermissionsForUserAndDocument(Long userId, Long documentId) {
        QueryWrapper<Documentpermissions> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("document_id", documentId);
        List<Long> permissionIds = documentpermissionsMapper.selectList(wrapper).stream()
                .map(Documentpermissions::getPermissionId)
                .collect(Collectors.toList());

        QueryWrapper<Permissions> permissionsWrapper = new QueryWrapper<>();
        permissionsWrapper.in("permission_id", permissionIds);
        return permissionsMapper.selectList(permissionsWrapper);
    }


}




