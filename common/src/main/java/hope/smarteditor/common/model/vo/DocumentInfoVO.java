package hope.smarteditor.common.model.vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DocumentInfoVO implements Serializable {
    private Long id;

    private Long userId;

    private String name;

    private String content;

    private String summary;

    private Integer type;

    private String label;

    private String subject;

    private String category;

    private Integer status;

    @TableLogic
    private Integer isDeleted;

    private Integer likeCount;

    private Integer visibility;

    private String createUserNickname; //创建者的昵称

    private Boolean isFavorite; //是否收藏

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
