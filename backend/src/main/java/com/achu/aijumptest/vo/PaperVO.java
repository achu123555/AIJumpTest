package com.achu.aijumptest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.vo.PaperVO
 *
 * @author: achu_code
 * description: Paper View Object
 */

public class PaperVO {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Detail extends BaseVO{
        @Schema(description = "试卷名称")
        private String name;
        @Schema(description = "试卷描述")
        private String description;
        @Schema(description = "试卷状态",example = "DRAFT",allowableValues = {"DRAFT","PUBLISH",""})
        private String status;
        @Schema(description = "试卷总分")
        private Double totalScore;
        @Schema(description = "试卷中的题目数量")
        private Integer questionCount;
        @Schema(description = "试卷发布的持续时间")
        private Integer duration;
        @Schema(description = "试卷题目,包含答案与选项")
        private List<QuestionDetailVO> questions;
    }
}
