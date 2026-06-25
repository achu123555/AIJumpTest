package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.AuthDTO;
import com.achu.aijumptest.vo.AuthVO;

/**
 * 认证服务。
 */
public interface AuthService {

    /**
     * 用户登录，成功后返回 JWT。
     */
    AuthVO.LoginResult login(AuthDTO.Login loginDTO);

    /**
     * 学生注册。
     */
    AuthVO.LoginResult register(AuthDTO.Register registerDTO);

    /**
     * 获取当前登录用户信息。
     */
    AuthVO.LoginUserInfo me();
}
