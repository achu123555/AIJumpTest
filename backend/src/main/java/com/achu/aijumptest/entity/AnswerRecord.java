package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.AnswerRecord
 *
 * @author: achu_code
 * description: 答题详情记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "answer_record")
@Schema(description = "答题详情记录")
public class AnswerRecord extends BaseEntity<Integer> {

    @Schema(description = "考试记录ID", example = "41")
    private Integer examRecordId;

    @Schema(description = "题目ID", example = "66")
    private Long questionId; //顺应系统其他表，题目ID继续使用标准的 Long 类型

    @Schema(description = "用户提交的答案", example = "A")
    private String userAnswer;

    @Schema(description = "题目得分", example = "5")
    private Integer score;

    @Schema(description = "是否正确（0:错误，1:正确）", example = "1")
    private Integer isCorrect;

    @Schema(description = "AI批阅/批改意见", example = "答案与题目要求不符，完全未提及核心考点...")
    private String aiCorrection;
}