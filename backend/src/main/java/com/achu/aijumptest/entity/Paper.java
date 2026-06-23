package com.achu.aijumptest.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.Paper
 *
 * @author: achu_code
 * description: 试卷实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Paper extends BaseEntity{

    @Schema(description = "试卷名称")
    private String name;
    @Schema(description = "试卷描述")
    private String description;
    @Schema(description = "试卷状态",example = "DRAFT",allowableValues = {"DRAFT","PUBLISHED",""})
    private String status;
    @Schema(description = "试卷总分")
    private Double totalScore;
    @Schema(description = "试卷中的题目数量")
    private Integer questionCount;
    @Schema(description = "试卷考试时长(单位:分钟)",example = "120",
            minimum = "1",maximum = "600")
    private Integer duration;
}
