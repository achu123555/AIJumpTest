package com.achu.aijumptest.dto;

import com.achu.aijumptest.entity.QuestionAnswer;
import com.achu.aijumptest.entity.QuestionChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.dto.QuestionDTO
 *
 * @author: achu_code
 * description: 题目DTO
 */

public class QuestionDTO {

    @Data
    @Schema(description = "题目分页查询条件对象")
    public static class Query {

        @Schema(description = "分页当前页数，默认为1", example = "1")
        private Integer current = 1;

        @Schema(description = "分页一页显示数量，默认为10", example = "10")
        private Integer size = 10;

        @Schema(description = "单个分类id,点击二级分类用,根据分类筛选", example = "4")
        private Integer categoryId;

        @Schema(description = "难度筛选条件：EASY, MEDIUM, HARD", example = "MEDIUM")
        private String difficulty;

        @Schema(description = "类型筛选条件：CHOICE, JUDGE, TEXT", example = "CHOICE")
        private String type;

        @Schema(description = "关键词筛选条件,根据关键词模糊筛选", example = "Java")
        private String keyword;

        @Schema(description = "多个分类id,点击一级分类时用")
        private List<Long> categoryIds;
    }

    @Data
    public static class Count{
        private Long categoryId;
        private Long ct;
    }

    @Data
    public static class SaveAndUpdate {

        @Schema(description = "题目id",example = "66")
        private Long id;
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

    }

    @Data
    @Schema(description = "AI 生成题目请求对象")
    public static class AiGenerate {

        @Schema(description = "题目分类ID", example = "5")
        private Long categoryId;

        @Schema(description = "题目分类名称",example = "Java基础")
        private String categoryName;

        @Schema(description = "生成题型：CHOICE、JUDGE、TEXT", example = "CHOICE")
        private String type;

        @Schema(description = "是否多选，仅 CHOICE 选择题有效", example = "false")
        private Boolean multi;

        @Schema(description = "题目难度：EASY、MEDIUM、HARD", example = "MEDIUM")
        private String difficulty;

        @Schema(description = "题目分值", example = "5")
        private Integer score;

        @Schema(description = "生成数量", example = "5")
        private Integer count;

        @Schema(description = "额外要求", example = "题目适合初学者，不要太偏")
        private String requirement;
    }

}
