package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.constant.AuthorityConstant;
import hope.smarteditor.common.constant.ErrorCode;
import hope.smarteditor.common.exception.BusinessException;
import hope.smarteditor.common.exception.GlobalExceptionHandler;
import hope.smarteditor.common.model.dto.DocumentFolderDTO;
import hope.smarteditor.common.model.dto.FolderDTO;
import hope.smarteditor.common.model.dto.FolderPermissionUpdateDTO;
import hope.smarteditor.common.model.dto.FolderUpdateDTO;
import hope.smarteditor.common.model.entity.DocumentFolder;
import hope.smarteditor.common.model.entity.Folder;
import hope.smarteditor.common.result.Result;
import hope.smarteditor.document.annotation.HandleException;
import hope.smarteditor.document.mapper.DocumentFolderMapper;
import hope.smarteditor.document.service.FolderService;
import hope.smarteditor.document.mapper.FolderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.stereotype.Service;

/**
* @author LoveF
* @description 针对表【folder】的数据库操作Service实现
* @createDate 2024-06-08 14:02:31
*/
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
    implements FolderService{

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private DocumentFolderMapper documentFolderMapper;

    @Override
    public boolean createFolder(FolderDTO folderDTO) {
        try {
            Folder folder = new Folder();
            BeanUtils.copyProperties(folderDTO,folder);
            folder.setPermissions(AuthorityConstant.VIEW);
            int insert = folderMapper.insert(folder);
            return insert>0;
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }


    }

    @Override
    @HandleException
    public boolean deleteFolder(Long folderId) {
        int i = folderMapper.deleteById(folderId);
        return i > 0;
    }

    @Override
    @HandleException
    public boolean updateFolder(FolderUpdateDTO folderDTO) {
        Folder folder = new Folder();
        BeanUtils.copyProperties(folderDTO, folder);
        return folderMapper.updateById(folder) > 0;
    }

    @Override
    @HandleException
    public boolean setFolderPermission(FolderPermissionUpdateDTO folderDTO) {
        Folder folder = new Folder();
        BeanUtils.copyProperties(folderDTO, folder);
        folder.setPermissions(folderDTO.getPermission() == 1 ? AuthorityConstant.VIEW : AuthorityConstant.EDIT);
        int i = folderMapper.updateById(folder);
        return i > 0;
    }

    @Override
    @HandleException
    public boolean createDocument(Long folderId, Long documentId) {
        DocumentFolder documentFolder = new DocumentFolder();
        documentFolder.setFolderId(folderId);
        documentFolder.setDocumentId(documentId);
        int insert = documentFolderMapper.insert(documentFolder);
        return insert > 0;
    }

    @Override
    @HandleException
    public boolean deleteDocument(Long documentId) {
        int i = documentFolderMapper.delete(new QueryWrapper<DocumentFolder>().eq("document_id", documentId));
        return i > 0;
    }



}




