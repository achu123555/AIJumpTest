package com.achu.aijumptest.security;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.achu.aijumptest.enums.ResultCode;
import com.achu.aijumptest.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简单 JWT 工具类。
 *
 * 这里手动实现 HS256 JWT，避免再引入额外 JWT 依赖。
 * JWT结构：Base64Url(header).Base64Url(payload).signature
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties jwtProperties;

    /**
     * 生成登录 token。
     */
    public String generateToken(LoginUser loginUser) {
        long now = Instant.now().getEpochSecond();
        long exp = now + jwtProperties.getExpireMinutes() * 60;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", loginUser.getUserId());
        payload.put("username", loginUser.getUsername());
        payload.put("nickname", loginUser.getNickname());
        payload.put("role", loginUser.getRole());
        payload.put("iat", now);
        payload.put("exp", exp);

        String encodedHeader = base64Url(JSON.toJSONString(header));
        String encodedPayload = base64Url(JSON.toJSONString(payload));
        String data = encodedHeader + "." + encodedPayload;
        return data + "." + sign(data);
    }

    /**
     * 解析并校验 token，失败时抛 401。
     */
    public LoginUser parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "请先登录！");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无效 token！");
        }

        String data = parts[0] + "." + parts[1];
        String expectedSignature = sign(data);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "token 签名错误！");
        }

        JSONObject payload;
        try {
            payload = JSON.parseObject(new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "token 解析失败！");
        }

        Long exp = payload.getLong("exp");
        if (exp == null || exp < Instant.now().getEpochSecond()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "token 已过期，请重新登录！");
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(payload.getLong("userId"));
        loginUser.setUsername(payload.getString("username"));
        loginUser.setNickname(payload.getString("nickname"));
        loginUser.setRole(payload.getString("role"));
        return loginUser;
    }

    public long getExpireAtMillis() {
        return System.currentTimeMillis() + jwtProperties.getExpireMinutes() * 60 * 1000;
    }

    private String base64Url(String text) {
        return URL_ENCODER.encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            return URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("JWT签名生成失败！");
        }
    }
}
