package hope.smarteditor.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName api_calls
 */
@TableName(value ="api_calls")
@Data
public class ApiCalls implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 接口ID，外键，引用api_info.id
     */
    private Integer apiId;

    /**
     * 调用者
     */
    private Long userId;

    /**
     * 接口响应状态码
     */
    private Integer statusCode;

    /**
     * 记录创建时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
