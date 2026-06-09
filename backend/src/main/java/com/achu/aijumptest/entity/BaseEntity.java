package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * projectName: com.achu.aijumptest.pojo.BaseEntity
 *
 * @author: achu_code
 * description: 提取entity共同属性的父类
 */

@Data
public class BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO) //mp自增类型主键
    private Long id;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy:MM:dd HH:mm:ss",timezone = "GMT+8") //格式化时间和调整时区
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy:MM:dd HH:mm:ss",timezone = "GMT+8") //格式化时间和调整时区
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableLogic //开启mp逻辑删除
    @JsonIgnore //不返回和不接收该字段的json
    private Byte isDeleted;
}
