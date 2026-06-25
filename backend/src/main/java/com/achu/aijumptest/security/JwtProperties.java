package com.achu.aijumptest.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥。实际部署时建议改成长随机字符串，并放到环境变量里。
     */
    private String secret = "AIJumpTest-Change-Me-Secret-At-Least-32-Bytes";

    /**
     * token有效期，单位分钟。
     */
    private Long expireMinutes = 120L;
}
