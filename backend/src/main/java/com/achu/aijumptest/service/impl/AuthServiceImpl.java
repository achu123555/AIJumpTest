package com.achu.aijumptest.service.impl;

import com.achu.aijumptest.dto.AuthDTO;
import com.achu.aijumptest.entity.SysUser;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.SysUserMapper;
import com.achu.aijumptest.security.AuthContext;
import com.achu.aijumptest.security.JwtUtils;
import com.achu.aijumptest.security.LoginUser;
import com.achu.aijumptest.service.AuthService;
import com.achu.aijumptest.vo.AuthVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户表 + BCrypt + JWT 登录实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public AuthVO.LoginResult login(AuthDTO.Login loginDTO) {
        if (loginDTO == null || isBlank(loginDTO.getUsername()) || isBlank(loginDTO.getPassword())) {
            throw new BusinessException("账号和密码不能为空！");
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername().trim())
                        .last("LIMIT 1")
        );
        if (user == null) {
            throw new BusinessException("账号或密码错误！");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员！");
        }

        // BCrypt 校验：第一个参数是用户输入的明文，第二个参数是数据库中的密文。
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误！");
        }

        // 前端选择管理员/学生入口时，额外校验角色，避免学生从管理员入口登录。
        if (!isBlank(loginDTO.getRole()) && !loginDTO.getRole().trim().equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("当前账号不是" + roleName(loginDTO.getRole()) + "账号！");
        }

        return buildLoginResult(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthVO.LoginResult register(AuthDTO.Register registerDTO) {
        if (registerDTO == null || isBlank(registerDTO.getUsername()) || isBlank(registerDTO.getPassword())) {
            throw new BusinessException("注册账号和密码不能为空！");
        }
        String username = registerDTO.getUsername().trim();
        if (username.length() < 3 || username.length() > 32) {
            throw new BusinessException("账号长度必须为 3-32 个字符！");
        }
        if (registerDTO.getPassword().length() < 6 || registerDTO.getPassword().length() > 32) {
            throw new BusinessException("密码长度必须为 6-32 个字符！");
        }

        Long exists = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        if (exists != null && exists > 0) {
            throw new BusinessException("该账号已存在，请换一个账号！");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        // 学生注册接口只允许创建 STUDENT，管理员账号由数据库初始化，避免前端越权注册管理员。
        user.setRole("STUDENT");
        user.setStatus(1);
        user.setNickname(isBlank(registerDTO.getNickname()) ? username : registerDTO.getNickname().trim());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        sysUserMapper.insert(user);

        log.info("学生用户注册成功，userId={}，username={}", user.getId(), user.getUsername());
        return buildLoginResult(user);
    }

    @Override
    public AuthVO.LoginUserInfo me() {
        LoginUser loginUser = AuthContext.get();
        if (loginUser == null) {
            throw new BusinessException("请先登录！");
        }
        AuthVO.LoginUserInfo info = new AuthVO.LoginUserInfo();
        info.setUserId(loginUser.getUserId());
        info.setUsername(loginUser.getUsername());
        info.setNickname(loginUser.getNickname());
        info.setRole(loginUser.getRole());
        return info;
    }

    private AuthVO.LoginResult buildLoginResult(SysUser user) {
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
        String token = jwtUtils.generateToken(loginUser);

        AuthVO.LoginResult result = new AuthVO.LoginResult();
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setNickname(user.getNickname());
        result.setRole(user.getRole());
        result.setToken(token);
        result.setExpireAt(jwtUtils.getExpireAtMillis());
        return result;
    }

    private String roleName(String role) {
        return "ADMIN".equalsIgnoreCase(role) ? "管理员" : "学生";
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
