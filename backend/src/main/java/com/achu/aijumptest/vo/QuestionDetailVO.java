package com.achu.aijumptest.vo;

import com.achu.aijumptest.entity.QuestionAnswer;
import com.achu.aijumptest.entity.QuestionChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.vo.QuestionDetailVO
 *
 * @author: achu_code
 * description: 查询的题目结果
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionDetailVO extends BaseVO{

    @Schema(description = "题目", example = "以下关于Java面向对象编程说法正确的是？")
    private String title;
    @Schema(description = "题目类型", example = "CHOICE", allowableValues = {"CHOICE","JUDGE","TEXT"})
    private String type;
    @Schema(description = "关联分类表id", example = "1")
    private Long categoryId;
    @Schema(description = "是否为多选题,仅选择题有效",example = "false")
    private boolean multi;
    @Schema(description = "题目难度", example = "MEDIUM", allowableValues = {"EASY","MEDIUM","HARD"})
    private String difficulty;
    @Schema(description = "题目默认分值", example = "5")
    private Integer score;
    @Schema(description = "题目解析", example = "Java是面向对象编程语言，支持封装、继承、多态三大特性...")
    private String analysis;
    @Schema(description = "题目答案")
    private QuestionAnswer questionAnswer;
    @Schema(description = "题目选项列表")
    private List<QuestionChoice> questionChoiceList;
    @Schema(description = "试卷题目中间表中的题目真实分数,没有时才用题目默认分数")
    private BigDecimal realScore;
}
