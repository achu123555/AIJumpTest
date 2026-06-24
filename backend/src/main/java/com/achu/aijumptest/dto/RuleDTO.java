package com.achu.aijumptest.dto;

import com.achu.aijumptest.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.dto.RuleDTO
 *
 * @author: achu_code
 * description:
 */

@Data
@Schema(description = "组卷规则数据传输对象")
public class RuleDTO {

    @Schema(description = "题目类型",
            example = "CHOICE",
            allowableValues = {"CHOICE","JUDGE","TEXT"})
    private QuestionType type;
    @Schema(description = "指定的题目的分类id列表,为空则不限制分类",
            example = "[1,2,3]")
    private List<Integer> categoryIds;
    @Schema(description = "要抽取的题目数量",example = "10",minimum = "1")
    private Integer count;
    @Schema(description = "每道题的分值",example = "5",minimum = "1")
    private Integer score;
}
