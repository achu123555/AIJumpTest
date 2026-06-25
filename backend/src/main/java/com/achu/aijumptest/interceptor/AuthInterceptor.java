package com.achu.aijumptest.interceptor;

import com.achu.aijumptest.enums.ResultCode;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.security.AuthContext;
import com.achu.aijumptest.security.JwtUtils;
import com.achu.aijumptest.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 权限拦截器。
 *
 * 权限规则：
 * 1. ADMIN：可以访问全部后台和学生端接口。
 * 2. STUDENT：只能访问学生端接口，不能访问 /api/papers、/api/banners、管理类增删改接口等。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method) || isWhiteList(uri)) {
            return true;
        }

        // 支持标准 Authorization: Bearer xxx，也兼容旧版 X-Auth-Token。
        String token = getToken(request);
        LoginUser loginUser = jwtUtils.parseToken(token);
        AuthContext.set(loginUser);

        if ("ADMIN".equals(loginUser.getRole())) {
            return true;
        }

        if (!"STUDENT".equals(loginUser.getRole())) {
            AuthContext.clear();
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无效用户角色！");
        }

        if (isStudentAllowed(uri, method)) {
            return true;
        }

        AuthContext.clear();
        throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "学生用户无权访问管理端接口！");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Tomcat线程会复用，请求结束必须清理当前用户，避免串号。
        AuthContext.clear();
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Auth-Token");
    }

    private boolean isWhiteList(String uri) {
        return uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/register")
                // 首页公开展示使用：未登录也允许读取启用轮播图和热门题目。
                || uri.startsWith("/api/banners/active")
                || uri.startsWith("/api/questions/popular")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/webjars")
                || uri.startsWith("/doc.html")
                || uri.equals("/")
                || uri.equals("/favicon.ico");
    }

    private boolean isStudentAllowed(String uri, String method) {
        // 学生端考试接口。
        // /api/exam/records 是后台成绩管理接口，必须拦截。
        if (uri.startsWith("/api/exam/records")) {
            return false;
        }
        if (uri.startsWith("/api/exam")) {
            return true;
        }

        // 学生端题库练习只允许 GET 查询。
        if ("GET".equalsIgnoreCase(method) && uri.startsWith("/api/questions")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && uri.startsWith("/api/categories")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && uri.startsWith("/api/banners/active")) {
            return true;
        }

        return false;
    }
}
