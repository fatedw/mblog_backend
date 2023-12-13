package st.coo.memo.dto.user;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String displayName;


    private String email;


    private String bio;


    private String avatarUrl;


    private String password;


    private String defaultVisibility;


    private String defaultEnableComment;
}
