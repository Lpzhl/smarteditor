package hope.smarteditor.common.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FolderOperationLogVO implements Serializable {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 文件夹ID
     */
    private Long folderId;

    /**
     * 文档ID，若涉及文件操作则记录文档ID
     */
    private Long documentId;

    /**
     * 文档名，若涉及文件操作则记录文档名
     */
    private String documentName;

    /**
     * 操作类型（创建、插入、删除、更新）
     */
    private String operation;



    /**
     * 操作时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;
}
