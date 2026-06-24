package com.achu.aijumptest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * projectName: com.achu.aijumptest.vo.AiJudgeVO
 * description: AI 批阅结果 VO
 */
public class AiJudgeVO {

    @Data
    @Schema(description = "AI 简答题批阅结果")
    public static class TextJudgeResult {
        @Schema(description = "本题得分")
        private Integer score;

        @Schema(description = "AI 批阅意见")
        private String correction;
    }
}
