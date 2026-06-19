package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.vo.QuestionPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.QuestionService
 *
 * @author: achu_code
 * description: 题目业务层
 */

public interface QuestionService extends IService<Question> {

    /**
     * 分页查询题目 1+N
     * @param queryDTO 分页参数和查询条件
     * @return 分页查询结果
     */
    Page<QuestionPageVO> queryByPage(QuestionDTO.Query queryDTO);

    /**
     * 增强分页查询 ：java层面操作,把数据拉到内存中处理 1+1+1
     * @param queryDTO 分页参数和查询条件
     * @return 分页查询结果
     */
    Page<QuestionPageVO> queryByPageEnhance(QuestionDTO.Query queryDTO);

    /**
     * 保存题目
     * @param saveAndUpdateDTO 题目传输数据对象
     */
    void save(QuestionDTO.SaveAndUpdate saveAndUpdateDTO);

    /**
     * 根据id获取题目的详情信息,包括答案和选项
     * @param id 题目id
     * @return 题目展示对象
     */
    QuestionPageVO getById(Long id);

    /**
     * 更新题目的详情信息接口,包括答案和选项
     * @param updateDTO 更新数据传输DTO
     */
    void update(QuestionDTO.SaveAndUpdate updateDTO);

    /**
     * 根据传入的size查找size条热门题目
     * @param size 查找的热门题目数量
     * @return 完整题目列表
     */
    List<QuestionPageVO> getPopularQuestion(Integer size);

    /**
     * 根据题目id删除题目及其选项与答案
     * @param id 题目id
     */
    void delete(Long id);
}
