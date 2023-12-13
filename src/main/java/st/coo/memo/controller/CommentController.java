package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.comment.QueryCommentListRequest;
import st.coo.memo.dto.comment.QueryCommentListResponse;
import st.coo.memo.dto.comment.SaveCommentRequest;
import st.coo.memo.service.CommentService;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/add")

    public ResponseDTO<Void> addComment(@RequestBody SaveCommentRequest saveCommentRequest) {
        commentService.addComment(saveCommentRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/remove")
    @SaCheckLogin

    public ResponseDTO<Void> remove(@RequestParam("id") int id) {
        commentService.removeComment(id);
        return ResponseDTO.success();
    }


    @PostMapping("/query")

    public ResponseDTO<QueryCommentListResponse> query(@RequestBody QueryCommentListRequest request) {
        return ResponseDTO.success(commentService.query(request));
    }

    @PostMapping("/singleApprove")

    public ResponseDTO<QueryCommentListResponse> singleApprove(@RequestParam("id") int id) {
        commentService.singleApprove(id);
        return ResponseDTO.success();
    }

    @PostMapping("/memoApprove")

    public ResponseDTO<QueryCommentListResponse> memoApprove(@RequestParam("id") int id) {
        commentService.memoApprove(id);
        return ResponseDTO.success();
    }
}
