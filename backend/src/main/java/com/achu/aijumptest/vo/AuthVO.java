package com.achu.aijumptest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录返回 VO。
 */
public class AuthVO {

    @Data
    @Schema(description = "登录成功返回信息")
    public static class LoginResult {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "访问令牌 JWT")
        private String token;

        @Schema(description = "登录角色：ADMIN / STUDENT")
        private String role;

        @Schema(description = "登录账号")
        private String username;

        @Schema(description = "显示昵称/考生姓名")
        private String nickname;

        @Schema(description = "token过期时间戳，单位毫秒")
        private Long expireAt;
    }

    @Data
    @Schema(description = "当前登录用户信息")
    public static class LoginUserInfo {
        private Long userId;
        private String username;
        private String nickname;
        private String role;
    }
}
