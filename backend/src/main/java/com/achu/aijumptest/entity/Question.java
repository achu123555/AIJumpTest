package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.Question
 *
 * @author: achu_code
 * description: 题目实体类
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("questions")
public class Question extends BaseEntity{

    @Schema(description = "关联分类表id")
    private Long categoryId;

    @Schema(description = "问题")
    private String context;

    @Schema(description = "选项")
    private String options;

    @Schema(description = "答案")
    private String answer;

    @Schema(description = "分析")
    private String analysis;

    @Schema(description = "难度")
    private boolean difficulty;

    @Schema(description = "评分")
    private Integer defaultScore;

}
