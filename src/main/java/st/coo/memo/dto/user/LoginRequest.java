package st.coo.memo.dto.user;

import lombok.Data;

@Data

public class LoginRequest {
    private String username;

    private String password;
}
