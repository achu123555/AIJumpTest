package com.achu.aijumptest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * projectName: com.achu.aijumptest.enums.ResultCode
 *
 * @author: achu_code
 * description: 返回结果状态码枚举类
 */

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 token 已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;
}
