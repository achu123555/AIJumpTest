package com.achu.aijumptest.security;

/**
 * 当前请求登录用户上下文。
 *
 * 注意：
 * ThreadLocal 必须在请求结束后 clear，避免 Tomcat 线程复用导致用户信息串号。
 */
public final class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = get();
        return user == null ? null : user.getUserId();
    }

    public static String getRole() {
        LoginUser user = get();
        return user == null ? null : user.getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
