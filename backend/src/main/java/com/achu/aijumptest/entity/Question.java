package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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

    @Schema(description = "题目", example = "以下关于Java面向对象编程说法正确的是？")
    private String title;

    @Schema(description = "题目类型", example = "CHOICE", allowableValues = {"CHOICE","JUDGE","TEXT"})
    private String type;

    @Schema(description = "关联分类表id", example = "1")
    private Long categoryId;

    @Schema(description = "是否为多选题,仅选择题有效",example = "false")
    private Boolean multi;

    @Schema(description = "题目难度", example = "MEDIUM", allowableValues = {"EASY","MEDIUM","HARD"})
    private String difficulty;

    @Schema(description = "题目默认分值", example = "5")
    private Integer score;

    @Schema(description = "题目解析,详细的答案说明", example = "Java是面向对象编程语言，支持封装、继承、多态三大特性...")
    private String analysis;
}
