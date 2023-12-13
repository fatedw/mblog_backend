package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.memo.*;
import st.coo.memo.service.MemoService;

@Slf4j
@RestController
@RequestMapping("/api/memo")

public class MemoController {

    @Resource
    private MemoService memoService;

    @PostMapping("/save")
    @SaCheckLogin

    public ResponseDTO<Integer> create(@RequestBody  SaveMemoRequest saveMemoRequest) {
        return ResponseDTO.success(memoService.save(saveMemoRequest));
    }

    @PostMapping("/update")
    @SaCheckLogin

    public ResponseDTO<Integer> update(@RequestBody  SaveMemoRequest updateMemoRequest) {
        return ResponseDTO.success(memoService.update(updateMemoRequest));
    }

    @PostMapping("/remove")
    @SaCheckLogin

    public ResponseDTO<Void> remove(@RequestParam("id") int id) {
        memoService.remove(id);
        return ResponseDTO.success();
    }

    @PostMapping("/setPriority")
    @SaCheckLogin

    public ResponseDTO<Void> setTop(@RequestParam("id") int id,
                                    @RequestParam("set") boolean set) {
        memoService.setMemoPriority(id, set);
        return ResponseDTO.success();
    }

    @PostMapping("/{id}")

    public ResponseDTO<MemoDto> get(@PathVariable("id") int id, @RequestParam(name = "count", defaultValue = "false") boolean count) {
        return ResponseDTO.success(memoService.get(id, count));
    }

    @PostMapping("/list")

    public ResponseDTO<ListMemoResponse> list(@RequestBody  ListMemoRequest listMemoRequest) {
        return ResponseDTO.success(memoService.listNormal(listMemoRequest));
    }

    @PostMapping("/statistics")

    public ResponseDTO<StatisticsResponse> statistics(@RequestBody  StatisticsRequest statisticsRequest) {
        return ResponseDTO.success(memoService.statistics(statisticsRequest));
    }

    @PostMapping("/relation")
    @SaCheckLogin

    public ResponseDTO<Void> relation(@RequestBody MemoRelationRequest request) {
        memoService.makeRelation(request);
        return ResponseDTO.success();
    }


}
