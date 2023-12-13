package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.memo.MemoStatisticsDto;
import st.coo.memo.dto.user.*;
import st.coo.memo.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")

public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")

    public ResponseDTO<Void> register(@RequestBody  RegisterUserRequest registerUserRequest) {
        if (StringUtils.isEmpty(registerUserRequest.getUsername()) || StringUtils.isEmpty(registerUserRequest.getPassword())) {
            return ResponseDTO.fail(ResponseCode.param_error.getCode(), "用户名和密码不能为空");
        }
        userService.register(registerUserRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/update")
    @SaCheckLogin()

    public ResponseDTO<Void> update(@RequestBody  UpdateUserRequest updateUserRequest) {
        userService.update(updateUserRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/{id}")
    @SaCheckLogin

    public ResponseDTO<UserDto> get(@PathVariable("id") int id) {
        return ResponseDTO.success(userService.get(id));
    }

    @PostMapping("/current")

    public ResponseDTO<UserDto> current() {
        return ResponseDTO.success(userService.current());
    }


    @PostMapping("/list")
    @SaCheckRole(value = "ADMIN")

    public ResponseDTO<List<UserDto>> list() {
        return ResponseDTO.success(userService.list());
    }

    @PostMapping("/login")

    public ResponseDTO<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        if (StringUtils.isEmpty(loginRequest.getUsername()) || StringUtils.isEmpty(loginRequest.getPassword())) {
            return ResponseDTO.fail(ResponseCode.param_error.getCode(), "用户名和密码不能为空");
        }
        return ResponseDTO.success(userService.login(loginRequest));
    }

    @PostMapping("/logout")
    @SaCheckLogin

    public ResponseDTO<Void> logout() {
        userService.logout();
        return ResponseDTO.success();
    }

    @PostMapping("/listNames")
    @SaCheckLogin()

    public ResponseDTO<List<String>> listNames() {
        return ResponseDTO.success(userService.listNames());
    }


    @PostMapping("/statistics")

    public ResponseDTO<MemoStatisticsDto> getMentionedCount() {
        return ResponseDTO.success(userService.statistics());
    }

}
