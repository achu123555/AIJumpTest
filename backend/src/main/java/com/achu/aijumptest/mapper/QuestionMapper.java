package com.achu.aijumptest.mapper;

import com.achu.aijumptest.dto.CategoryQuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.vo.CategoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * projectName: com.achu.aijumptest.mapper.QuestionMapper
 *
 * @author: achu_code
 * description: 题目数据传输层
 */

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查出所有分类对应的题目数量 (只统计未删除的题目)
     * @return  Map<分类id,题目数量>
     */
    @Select("SELECT category_id categoryId,COUNT(*) ct FROM questions " +
            "WHERE is_deleted = 0 GROUP BY category_id")
    @MapKey("category_id")
    List<CategoryQuestionDTO> selectQuestionCount();
}
