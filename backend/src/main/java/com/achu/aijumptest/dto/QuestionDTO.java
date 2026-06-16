package com.achu.aijumptest.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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

        @Schema(description = "分类ID筛选条件，根据分类筛选", example = "4")
        private Integer categoryId;

        @Schema(description = "难度筛选条件：EASY, MEDIUM, HARD", example = "MEDIUM")
        private String difficulty;

        @Schema(description = "类型筛选条件：CHOICE, JUDGE, TEXT", example = "CHOICE")
        private String type;

        @Schema(description = "关键词筛选条件,根据关键词模糊筛选", example = "Java")
        private String keyword;
    }

    @Data
    public static class Count{
        private Long categoryId;
        private Long ct;
    }

}
