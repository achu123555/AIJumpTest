package com.achu.aijumptest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录注册相关 DTO。
 */
public class AuthDTO {

    @Data
    @Schema(description = "登录请求")
    public static class Login {
        @Schema(description = "登录角色：ADMIN / STUDENT，用于校验登录入口", example = "STUDENT")
        private String role;

        @Schema(description = "登录账号", example = "student")
        private String username;

        @Schema(description = "登录密码", example = "student123")
        private String password;
    }

    @Data
    @Schema(description = "学生注册请求")
    public static class Register {
        @Schema(description = "登录账号", example = "student001")
        private String username;

        @Schema(description = "密码", example = "student123")
        private String password;

        @Schema(description = "显示昵称/考生姓名", example = "张三")
        private String nickname;
    }
}
