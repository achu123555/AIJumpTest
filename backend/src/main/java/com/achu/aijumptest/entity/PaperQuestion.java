package com.achu.aijumptest.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.PaperQuestion
 *
 * @author: achu_code
 * description: 问题与试卷中间表实体类
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class PaperQuestion extends BaseEntity{

    @Schema(description = "试卷id")
    private Integer paperId;
    @Schema(description = "题目id")
    private Long questionId;
    @Schema(description = "分数")
    private Double score;

}
