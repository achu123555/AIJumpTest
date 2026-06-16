package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.QuestionChoice
 *
 * @author: achu_code
 * description: 题目选项实体类
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("question_choices")
public class QuestionChoice extends BaseEntity{

    @Schema(description = "关联的问题id",example = "66")
    private Long questionId;
    @Schema(description = "该选项的内容",example = "ArrayList适合随机查找，LinkedList适合头尾快速插入与删除")
    private String content;
    @Schema(description = "该选项是否正确",example = "true")
    private boolean isCorrect;
    @Schema(description = "该选项的排序优先级",example = "1")
    private Integer sort;

}
