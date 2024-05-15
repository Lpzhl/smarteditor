package hope.smarteditor.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录信息返回的token")
public class UserLoginVO implements Serializable {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户openid")
    private String openid;

    @ApiModelProperty("用户token")
    private String token;

}