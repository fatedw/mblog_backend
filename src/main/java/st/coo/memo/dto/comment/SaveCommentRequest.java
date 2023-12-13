package st.coo.memo.dto.comment;

import lombok.Data;

@Data

public class SaveCommentRequest {

    private String content;

    private int memoId;

    private String username;
    private String email;
    private String link;

}
