package st.coo.memo.dto.memo;

import lombok.Data;
import st.coo.memo.common.Visibility;

import java.util.Date;

@Data
public class ListMemoRequest {

    private int page = 1;


    private int size = 20;


    private String tag;


    private Visibility visibility;


    private int userId;


    private int currentUserId;


    private Date begin;


    private Date end;


    private String search;


    private boolean login;


    private boolean liked;


    private boolean commented;


    private boolean mentioned;


    private String dbType;
}
