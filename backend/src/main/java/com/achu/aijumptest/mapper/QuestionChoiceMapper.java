package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.QuestionChoice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.mapper.QuestionChoiceMapper
 *
 * @author: achu_code
 * description: 题目选项数据访问层
 */

@Mapper
public interface QuestionChoiceMapper extends BaseMapper<QuestionChoice> {

    /**
     * 根据题目id查询选项
     * @param id 题目id
     * @return 选项集合
     */
    @Select("SELECT * FROM question_choices WHERE is_deleted = 0 AND question_id = #{id} ORDER BY sort")
    List<QuestionChoice> selectChoiceByQuestionId(@Param("id") Long id);
}
