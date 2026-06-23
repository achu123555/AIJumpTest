package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.QuestionAnswer
 *
 * @author: achu_code
 * description: 问题答案实体类
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionAnswer extends BaseEntity<Long>{

    @Schema(description = "关联的题目id")
    private Long questionId;
    @Schema(description = "答案",example = "true",allowableValues = {"true","A","A,C","缓存穿透指查询根本不存在的数据"})
    private String answer;
    @Schema(description = "关键词")
    private String keywords;
}
