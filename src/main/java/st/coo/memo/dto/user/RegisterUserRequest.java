package st.coo.memo.dto.user;

import lombok.Data;

@Data
public class RegisterUserRequest {

    private String username;


    private String password;


    private String displayName;


    private String email;


    private String bio;
}
