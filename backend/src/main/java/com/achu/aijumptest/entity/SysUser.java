package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户表实体。
 *
 * 说明：
 * 1. 管理员和学生用户统一存储在 sys_user 表。
 * 2. password 存储 BCrypt 加密后的密文，禁止把明文密码入库。
 * 3. role 用于权限判断：ADMIN 管理员，STUDENT 学生。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser extends BaseEntity<Long> {

    @Schema(description = "登录账号")
    private String username;

    @JsonIgnore
    @Schema(description = "BCrypt加密后的密码")
    private String password;

    @Schema(description = "显示昵称/考生姓名")
    private String nickname;

    @Schema(description = "用户角色：ADMIN / STUDENT")
    private String role;

    @Schema(description = "账号状态：1启用，0禁用")
    private Integer status;
}
