package com.achu.aijumptest.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * projectName: com.achu.aijumptest.vo.BaseVO
 *设计思想：传输给前端的通用数据载体，不需要传逻辑删除字段
 * @author: achu_code
 * description: VO通用参数类
 */
@Data

public class BaseVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime updateTime;

}
