package com.achu.aijumptest.mapper;

import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


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
    List<QuestionDTO.Count> selectQuestionCount();

    /**
     * 分页+条件查询 → 题目+答案+选项
     * 形参说明：第一个参数必须是 Page对象，MP 自动识别
     * @return 分页对象
     */
    IPage<QuestionDetailVO> selectByPage(
            @Param("page") Page<QuestionDetailVO> page,
            @Param("query") QuestionDTO.Query queryDTO);


}
