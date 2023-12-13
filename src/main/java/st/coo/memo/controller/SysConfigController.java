package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.sysConfig.SaveSysConfigRequest;
import st.coo.memo.dto.sysConfig.SysConfigDto;
import st.coo.memo.service.SysConfigService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sysConfig")

public class SysConfigController {


    @Resource
    private SysConfigService sysConfigService;

    @PostMapping("/save")
    @SaCheckRole("ADMIN")

    public ResponseDTO<Void> save(@RequestBody SaveSysConfigRequest saveSysConfigRequest) {
        if (saveSysConfigRequest == null || CollectionUtils.isEmpty(saveSysConfigRequest.getItems())) {
            return ResponseDTO.fail(ResponseCode.param_error);
        }
        sysConfigService.save(saveSysConfigRequest);
        return ResponseDTO.success();
    }

    @GetMapping("/get")
    @SaCheckRole("ADMIN")

    public ResponseDTO<List<SysConfigDto>> get() {
        return ResponseDTO.success(sysConfigService.getAll());
    }

    @GetMapping("/")

    public ResponseDTO<List<SysConfigDto>> getConfig() {
        List<String> keys = List.of(SysConfigConstant.OPEN_REGISTER,
                SysConfigConstant.WEBSITE_TITLE,
                SysConfigConstant.OPEN_COMMENT,
                SysConfigConstant.OPEN_LIKE,
                SysConfigConstant.MEMO_MAX_LENGTH,
                SysConfigConstant.INDEX_WIDTH,
                SysConfigConstant.USER_MODEL,
                SysConfigConstant.CUSTOM_CSS,
                SysConfigConstant.CUSTOM_JAVASCRIPT,
                SysConfigConstant.THUMBNAIL_SIZE,
                SysConfigConstant.ANONYMOUS_COMMENT,
                SysConfigConstant.COMMENT_APPROVED

        );
        return ResponseDTO.success(sysConfigService.getAll(keys));
    }
}
