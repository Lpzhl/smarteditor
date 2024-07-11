package hope.smarteditor.common.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class ElementDTO implements Serializable {

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

}
