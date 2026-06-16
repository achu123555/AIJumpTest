package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.entity.QuestionAnswer;
import com.achu.aijumptest.entity.QuestionChoice;
import com.achu.aijumptest.mapper.QuestionAnswerMapper;
import com.achu.aijumptest.mapper.QuestionChoiceMapper;
import com.achu.aijumptest.mapper.QuestionMapper;
import com.achu.aijumptest.service.QuestionService;
import com.achu.aijumptest.vo.QuestionPageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * projectName: com.achu.aijumptest.service.impl.QuestionServiceImpl
 *
 * @author: achu_code
 * description: 题目业务层实现类
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionAnswerMapper questionAnswerMapper;
    private final QuestionChoiceMapper questionChoiceMapper;


    @Override
    public Page<QuestionPageVO> queryByPage(QuestionDTO.Query queryDTO) {
        Page<QuestionPageVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        questionMapper.selectByPage(page,queryDTO);
        return page;
    }

    @Override
    public Page<QuestionPageVO> queryByPageEnhance(QuestionDTO.Query queryDTO) {
        //1.分页+条件查询所有题目
        Page<Question> pageBean = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!ObjectUtils.isEmpty(queryDTO.getCategoryId()),
                Question::getCategoryId,queryDTO.getCategoryId());
        queryWrapper.eq(!ObjectUtils.isEmpty(queryDTO.getType()),
                Question::getType,queryDTO.getType());
        queryWrapper.eq(!ObjectUtils.isEmpty(queryDTO.getDifficulty()),
                Question::getDifficulty,queryDTO.getDifficulty());
        queryWrapper.like(!ObjectUtils.isEmpty(queryDTO.getKeyword()),
                Question::getTitle,queryDTO.getKeyword());
        //查询参数：分页参数 和 包装条件
        page(pageBean, queryWrapper);
        //2.entity转成VO
        List<QuestionPageVO> voList = BeanUtil.copyToList(pageBean.getRecords(), QuestionPageVO.class);
        Page<QuestionPageVO> resultPage = new Page<>(pageBean.getCurrent(), pageBean.getSize(), pageBean.getTotal());
        resultPage.setRecords(voList);
        //3.提取公共方法批量填充答案和选项字段
        fillAnswerAndChoice(resultPage.getRecords());
        return resultPage;
    }

    private void fillAnswerAndChoice(List<QuestionPageVO> records) {
        //1.判空
        if(records == null || records.isEmpty()){
            log.debug("问题集合为空");
            return;
        }
        //2.根据题目id 查询所有答案和选项(性能好一些,不用一下全拉出来)
        List<Long> ids = records.stream()
                .map(QuestionPageVO::getId)
                .toList();
        List<QuestionAnswer> questionAnswers = questionAnswerMapper.selectList(new LambdaQueryWrapper<QuestionAnswer>()
                .in(QuestionAnswer::getQuestionId, ids));
        List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(new LambdaQueryWrapper<QuestionChoice>()
                .in(QuestionChoice::getQuestionId, ids));
        //3.答案和选项转化成map
        Map<Long, QuestionAnswer> answerMap = questionAnswers.stream()
                .collect(Collectors.toMap(
                        QuestionAnswer::getQuestionId,
                        questionAnswer -> questionAnswer
                ));
        Map<Long, List<QuestionChoice>> choiceMap = questionChoices.stream()
                .collect(Collectors.groupingBy(QuestionChoice::getQuestionId));
        //4.循环赋值
        records.forEach(questionPageVO -> {
            //每个题目肯定有答案
            questionPageVO.setQuestionAnswer(
                    answerMap.getOrDefault(questionPageVO.getId(),new QuestionAnswer()));
            //只有选择题才有选项
            if("CHOICE".equals(questionPageVO.getType())){
                List<QuestionChoice> choices = choiceMap.getOrDefault(questionPageVO.getId(),new ArrayList<>());
                choices.sort(Comparator.comparingInt(QuestionChoice::getSort));
                questionPageVO.setQuestionChoiceList(choices);
            }
        });

    }


}
