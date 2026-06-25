package com.achu.aijumptest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置。
 */
@Configuration
public class PasswordConfiguration {

    /**
     * BCrypt 是单向哈希算法，适合存储用户密码。
     * 入库只保存 encode 后的密文，登录时用 matches 校验。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
