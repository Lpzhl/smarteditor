package hope.smarteditor.common.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "用户登录请求")
public class UserLoginDTO implements Serializable {

    @ApiModelProperty("用户名")
    public String username;

    @ApiModelProperty("密码")
    public String password;
}