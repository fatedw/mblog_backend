package st.coo.memo.dto.comment;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CommentDto {


    private int id;

    private int memoId;

    private String userName;

    private int userId;

    private Timestamp created;

    private Timestamp updated;

    private String content;

    private String mentioned;

    private String mentionedUserId;

    private String email;

    private String link;

    private int approved;
}
