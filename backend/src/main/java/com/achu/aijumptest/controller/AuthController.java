package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.dto.AuthDTO;
import com.achu.aijumptest.service.AuthService;
import com.achu.aijumptest.vo.AuthVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 登录注册接口。
 *
 * 当前版本已切换为：用户表 + BCrypt密码加密 + JWT。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "登录接口", description = "从 sys_user 表查询用户，使用 BCrypt 校验密码，成功后返回 JWT")
    public Result<AuthVO.LoginResult> login(@RequestBody AuthDTO.Login loginDTO) {
        log.info("用户登录请求，username={}，role={}", loginDTO == null ? null : loginDTO.getUsername(), loginDTO == null ? null : loginDTO.getRole());
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    @Operation(summary = "学生注册接口", description = "公开注册只创建 STUDENT 账号；管理员账号通过数据库初始化")
    public Result<AuthVO.LoginResult> register(@RequestBody AuthDTO.Register registerDTO) {
        log.info("学生注册请求，username={}", registerDTO == null ? null : registerDTO.getUsername());
        return Result.success(authService.register(registerDTO));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息")
    public Result<AuthVO.LoginUserInfo> me() {
        return Result.success(authService.me());
    }
}
