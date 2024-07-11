package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 *
 * @TableName element
 */
@TableName(value ="element")
@Data
public class Element implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

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
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    /**
     * 使用次数
     */
    private Integer useCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
