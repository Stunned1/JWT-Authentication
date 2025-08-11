package anguy98.JWT_Authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    private String username;
    private String email;
    private String password;
}
