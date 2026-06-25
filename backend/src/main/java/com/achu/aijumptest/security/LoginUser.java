package com.achu.aijumptest.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息。
 *
 * 该对象会写入 JWT，也会在拦截器解析 token 后放到 ThreadLocal，
 * 后续业务层如果需要当前用户，可以通过 AuthContext 获取。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private String nickname;
    private String role;
}
