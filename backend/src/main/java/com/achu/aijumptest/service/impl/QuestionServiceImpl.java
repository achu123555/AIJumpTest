package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.entity.QuestionAnswer;
import com.achu.aijumptest.entity.QuestionChoice;
import com.achu.aijumptest.exception.BusinessException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        queryWrapper.orderByDesc(Question::getCreateTime);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(QuestionDTO.Save saveDTO) {
        //多表DML操作,需要开启事务,自定义异常继承的RuntimeException,不用设置rollback属性

        //1.先保存题目,再获取回显的id
        Question question = BeanUtil.copyProperties(saveDTO, Question.class);
        baseMapper.insert(question);

        Long questionId = question.getId();
        if(questionId == null){
            throw new BusinessException("题目保存失败,未获取到主键ID！");
        }

        //2.判断题型（简答题和判断题直接填充答案; 选择题需要保存拼接答案和选项）
        QuestionAnswer answer = saveDTO.getQuestionAnswer();
        if(ObjectUtils.isNotNull(answer)){
            // 2.1 简答题/判断题直接绑定题目ID并保存
            answer.setQuestionId(questionId);
            questionAnswerMapper.insert(answer);
        }else{
            // 2.2 选择题处理
            List<QuestionChoice> choices = saveDTO.getQuestionChoiceList();
            if(ObjectUtils.isNull(choices) || ObjectUtils.isEmpty(choices)){
                throw new BusinessException("非法数据,选择题必须提供选项列表！");
            }
            // 2.3 根据选项isCorrect字段拼接选择题&多选题答案(顺手批量设置选项关联的题目id)
            StringJoiner sj = new StringJoiner(",");
            for (QuestionChoice choice : choices) {
                choice.setQuestionId(questionId);
                if(choice.getIsCorrect()){
                    if(choice.getSort() == null){
                        throw new BusinessException("非法数据：选项的序号(sort)不能为空！");
                    }
                    int i = choice.getSort().intValue();
                    char correctAnswer = (char) (i + 'A' - 1);
                    sj.add(String.valueOf(correctAnswer));
                }
            }
            if(sj.length() == 0){
                throw new BusinessException("非法数据：选择题必须至少指定一个正确答案！");
            }
            
            //3.选择题/多选题答案
            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestionId(questionId);
            questionAnswer.setAnswer(sj.toString());

            //4.最后进行保存动作(避免代码后面发送异常回滚浪费数据库性能)
            questionChoiceMapper.insert(choices);
            questionAnswerMapper.insert(questionAnswer);
            }
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
