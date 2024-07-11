package hope.smarteditor.common.model.vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ElementVO implements Serializable {
    private Long id;

    /**
     * 用户id
     */
    private String nickname;

    /**
     * 素材名称
     */
    private String name;

    /**
     * 素材内容
     */
    private String content;

    /**
     * 素材种类
     */
    private String type;

    /**
     * 是否对外公开(0公开  1私密)
     */
    private Integer isPublic;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    /**
     * 使用次数
     */
    private Integer userCount;
}
