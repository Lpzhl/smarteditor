package hope.smarteditor.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDTO implements Serializable {
    private String username;

    private String password;

    private String email;

    private String nickname;


    private String inviteCode;

}
