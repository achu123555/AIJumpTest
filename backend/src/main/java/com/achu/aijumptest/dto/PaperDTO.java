package com.achu.aijumptest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * projectName: com.achu.aijumptest.dto.PaperDTO
 *
 * @author: achu_code
 * description: 试卷DTO
 */

public class PaperDTO {

    @Data
    @Schema(description = "创建试卷DTO")
    public static class Save implements Serializable {
        @Schema(description = "试卷名称",requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;
        @Schema(description = "试卷描述")
        private String description;
        @Schema(description = "考试时长(分钟)",example = "120",minimum = "1",maximum = "600")
        private Integer duration;
        @Schema(description = "试卷题目配置：key为题目ID,value为该题分数")
        private Map<Integer, BigDecimal> questions;

        @Serial
        private static final long serialVersionUID = 1L; //序列化版本UID
    }
}
