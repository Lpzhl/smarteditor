package hope.smarteditor.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hope.smarteditor.common.model.entity.Document;
import hope.smarteditor.common.model.entity.Share;
import hope.smarteditor.document.mapper.DocumentMapper;
import hope.smarteditor.document.service.ShareService;
import hope.smarteditor.document.mapper.ShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
* @author LoveF
* @description 针对表【share】的数据库操作Service实现
* @createDate 2024-07-05 17:20:22
*/
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share>
    implements ShareService{

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Override
    public Share shareDocument(Long documentId, Integer validDays, String editPermission) {
        // 1.查询是否已经存在该文档的分享链接
        Share existingShare = shareMapper.selectById(documentId);
        if (existingShare != null) {
            return existingShare;
        }

        // 2.创建新的分享链接
        Share share = new Share();
        share.setDocumentId(documentId);
        share.setLink(generateUniqueLink(documentId));
        share.setCreateTime(LocalDateTime.now());
        if (validDays != null) {
            share.setExpireTime(LocalDateTime.now().plusDays(validDays));
            share.setPermanent(0); // 链接有过期时间，设置为0
        } else {
            share.setExpireTime(null); // 永久有效，无过期时间
            share.setPermanent(1); // 设置为1，表示永久有效
        }
        share.setEditPermission(editPermission);
        shareMapper.insert(share);
        return share;
    }

    @Override
    public Share shareFolder(Long id, Integer validDays, String editPermission) {
        // 1.查询是否已经存在该文档的分享链接
        Share existingShare = shareMapper.selectById(id);
        if (existingShare != null) {
            return existingShare;
        }

        // 2.创建新的分享链接
        Share share = new Share();
        share.setFolderId(id);
        share.setLink(generateUniqueLink(id));
        share.setCreateTime(LocalDateTime.now());
        if (validDays != null) {
            share.setExpireTime(LocalDateTime.now().plusDays(validDays));
            share.setPermanent(0); // 链接有过期时间，设置为0
        } else {
            share.setExpireTime(null); // 永久有效，无过期时间
            share.setPermanent(1); // 设置为1，表示永久有效
        }
        share.setEditPermission(editPermission);
        shareMapper.insert(share);
        return share;
    }

    @Override
    public Document handleShareDocument(String link) {
        QueryWrapper<Share> shareQueryWrapper = new QueryWrapper<>();
        shareQueryWrapper.eq("link", link);
        Share share = shareMapper.selectOne(shareQueryWrapper);
        QueryWrapper<Document> documentQueryWrapper = new QueryWrapper<>();
        documentQueryWrapper.eq("id", share.getDocumentId());
        return documentMapper.selectOne(documentQueryWrapper);
    }


    private String generateUniqueLink(Long documentId) {
        return "http://yourdomain.com/share/"+UUID.randomUUID()+ "-" + documentId;
    }
}




