package st.coo.memo.dto.comment;

import lombok.Data;

@Data
public class QueryCommentListRequest {

    private int page;


    private int size;


    private int memoId;

}
