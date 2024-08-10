package hope.smarteditor.common.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName api_info
 */
@Data
public class ApiInfoDTO implements Serializable {

    private Integer id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 路径
     */
    private String path;

    /**
     * 1: 正常, 0: 下线
     */
    private Integer status;


}
