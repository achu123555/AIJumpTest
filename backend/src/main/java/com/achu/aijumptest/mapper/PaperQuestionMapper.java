package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.PaperQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * projectName: com.achu.aijumptest.mapper.PaperQuestionMapper
 *
 * @author: achu_code
 * description: 问题与试卷中间表持久层
 */

@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {
}
