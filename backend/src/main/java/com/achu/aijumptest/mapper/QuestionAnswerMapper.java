package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.QuestionAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * projectName: com.achu.aijumptest.mapper.QuestionAnswerMapper
 *
 * @author: achu_code
 * description: 题目答案数据访问层
 */
@Mapper
public interface QuestionAnswerMapper extends BaseMapper<QuestionAnswer> {
}
