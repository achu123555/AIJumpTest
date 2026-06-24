package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * projectName: com.achu.aijumptest.entity.Paper
 *
 * @author: achu_code
 * description: 试卷实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Paper extends BaseEntity<Integer>{

    @Schema(description = "试卷名称")
    private String name;
    @Schema(description = "试卷描述")
    private String description;
    @Schema(description = "试卷状态",example = "DRAFT",allowableValues = {"DRAFT","PUBLISHED","EXPIRE"})
    private String status;
    @Schema(description = "试卷总分")
    private BigDecimal totalScore;
    @Schema(description = "试卷中的题目数量")
    private Integer questionCount;
    @Schema(description = "试卷考试时长(单位:分钟)",example = "120",
            minimum = "1",maximum = "600")
    private Integer duration;

}
