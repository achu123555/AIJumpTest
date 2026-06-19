package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.common.CacheConstants;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.entity.QuestionAnswer;
import com.achu.aijumptest.entity.QuestionChoice;
import com.achu.aijumptest.enums.QuestionType;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.QuestionAnswerMapper;
import com.achu.aijumptest.mapper.QuestionChoiceMapper;
import com.achu.aijumptest.mapper.QuestionMapper;
import com.achu.aijumptest.service.QuestionService;
import com.achu.aijumptest.utils.RedisUtils;
import com.achu.aijumptest.vo.QuestionPageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    private final RedisUtils redisUtils;


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
    public void save(QuestionDTO.SaveAndUpdate saveAndUpdateDTO) {
        //多表DML操作,需要开启事务,自定义异常继承的RuntimeException,不用设置rollback属性

        //1.先保存题目,再获取回显的id
        Question question = BeanUtil.copyProperties(saveAndUpdateDTO, Question.class);
        baseMapper.insert(question);

        Long questionId = question.getId();
        if(questionId == null){
            throw new BusinessException("题目保存失败,未获取到主键ID！");
        }

        //2.判断题型（简答题和判断题直接填充答案; 选择题需要保存拼接答案和选项）
        QuestionAnswer answer = saveAndUpdateDTO.getQuestionAnswer();
        if(ObjectUtils.isNotNull(answer)){
            // 2.1 简答题/判断题直接绑定题目ID并保存
            answer.setQuestionId(questionId);
            questionAnswerMapper.insert(answer);
        }else{
            // 2.2 选择题处理
            List<QuestionChoice> choices = saveAndUpdateDTO.getQuestionChoiceList();
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

    @Override
    public QuestionPageVO getById(Long id) {
        //1.查出题目
        Question question = baseMapper.selectById(id);
        if(ObjectUtils.isNull(question)){
            throw new BusinessException("没有找到对应的题目！");
        }

        //2.拷贝题目,题目必有答案,直接查出答案填充
        QuestionPageVO questionPageVO = BeanUtil.copyProperties(question, QuestionPageVO.class);
        QuestionAnswer answer = questionAnswerMapper.selectOne(
                new LambdaQueryWrapper<QuestionAnswer>()
                        .eq(QuestionAnswer::getQuestionId, questionPageVO.getId())
                        .last("LIMIT 1") //防止并发Bug的脏数据导致抛异常
        );
        questionPageVO.setQuestionAnswer(answer);

        //3.根据题目类型去决定是否填充选项,只有选择题有选项
        String type = questionPageVO.getType();
        if(QuestionType.CHOICE.name().equals(type)){
            List<QuestionChoice> choices = questionChoiceMapper.selectList(
                    new LambdaQueryWrapper<QuestionChoice>()
                            .eq(QuestionChoice::getQuestionId, questionPageVO.getId())
            );
            questionPageVO.setQuestionChoiceList(choices);
        }

        //记录题目热度 ZINCRBY aijumptext:question:popular 1 题目ID
        redisUtils.zIncrementScore(CacheConstants.POPULAR_QUESTIONS_KEY, id, 1D);
        return questionPageVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(QuestionDTO.SaveAndUpdate updateDTO) {

        // 1.先判断题目是否重复(不同id同名题目 == 重复)
        boolean exists = baseMapper.exists(
                new LambdaQueryWrapper<Question>()
                        .ne(Question::getId, updateDTO.getId())
                        .eq(Question::getTitle, updateDTO.getTitle())
        );
        if(exists){
            throw new BusinessException("题目已存在,请勿重复提交！");
        }

        // 2.更新题目
        Question question = BeanUtil.copyProperties(updateDTO, Question.class);
        int update = baseMapper.updateById(question);
        if(update == 0){
            throw new BusinessException("更新失败,没有找到该题目!");
        }

        // 3.更新简答题判断题信息(答案为null就是选择题,不为null就是简答题和判断题)
        QuestionAnswer answer = updateDTO.getQuestionAnswer();
        if(ObjectUtils.isNotNull(answer)){
            //简答题有主键id,直接根据id更新即可
            questionAnswerMapper.updateById(answer);
            return;
        }

        // 4.更新选择题拼接答案和删除插入选项
        StringJoiner sj = new StringJoiner(",");
        List<QuestionChoice> choices = updateDTO.getQuestionChoiceList();
        for (QuestionChoice choice : choices) {
            choice.setQuestionId(question.getId());
            if(choice.getIsCorrect()){
                int i = choice.getSort().intValue();
                sj.add(String.valueOf((char)(i+'A'-1)));
            }
        }
        if(sj.length() == 0){
            throw new BusinessException("请设置一个或多个正确选项！");
        }

        answer.setAnswer(sj.toString());
        //没有id主键,有questionID → 根据条件更新
        questionAnswerMapper.update(
                null,
                new LambdaUpdateWrapper<QuestionAnswer>()
                        .eq(QuestionAnswer::getQuestionId,question.getId())
                        .set(QuestionAnswer::getAnswer,sj.toString())
        );
        //有questionID → 根据条件删除
        questionChoiceMapper.delete(
                new LambdaQueryWrapper<QuestionChoice>()
                        .eq(QuestionChoice::getQuestionId,question.getId())
        );
        questionChoiceMapper.insert(choices);
    }

    @Override
    public List<QuestionPageVO> getPopularQuestion(Integer size) {


        //1.从redis中查出6条分值最高的id
        Set<Object> idsSet = redisUtils.zReverseRange(
                CacheConstants.POPULAR_QUESTIONS_KEY,
                0,
                size - 1
        );

        //2.情况一：如果 Redis 此时是空的,直接去数据库保底拉取最新的size条
        if(idsSet == null || idsSet.isEmpty()){
            List<Question> questions = baseMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .orderByDesc(Question::getCreateTime)
                            .last("LIMIT " + size)
            );
            List<QuestionPageVO> voList = BeanUtil.copyToList(questions, QuestionPageVO.class);
            fillAnswerAndChoice(voList);
            return voList;
        }

        //3. 装填redis热门题目数据

        //3.1 将装id的Set<Object>转成List<Long>,再批量查出热门题目
        List<Long> idsList = idsSet.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        List<Question> redisQuestions = baseMapper.selectBatchIds(idsList);
        //3.2 因Mysql查询的是乱序的数据,需要自己排序
        Map<Long, Question> questionMap = redisQuestions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        List<Question> sortQuestions = new ArrayList<>();
        for (Long id : idsList) {
            Question q = questionMap.get(id);
            if(q != null){
                sortQuestions.add(q);
            }
        }
        //3.3 将排好序的热门题目，拷贝成 VO 并塞入总集合
        List<QuestionPageVO> popularQuestions = new ArrayList<>(BeanUtil.copyToList(sortQuestions, QuestionPageVO.class));

        //4. 情况二：如果redis中的热门题目不足size条,要再去数据库拉取最新的[size - idsList.size()]条
        int diff = size - idsSet.size();
        if(diff > 0){
            List<Question> questions = baseMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .notIn(Question::getId,idsList) //排除重复题目
                            .orderByDesc(Question::getCreateTime)
                            .last("LIMIT " + diff)
            );
            popularQuestions.addAll(BeanUtil.copyToList(questions, QuestionPageVO.class));
        }

        //5 数据齐了后统一插入答案或选项
        fillAnswerAndChoice(popularQuestions);

        return popularQuestions;
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
            if(QuestionType.CHOICE.name().equals(questionPageVO.getType())){
                List<QuestionChoice> choices = choiceMap.getOrDefault(questionPageVO.getId(),new ArrayList<>());
                choices.sort(Comparator.comparingInt(QuestionChoice::getSort));
                questionPageVO.setQuestionChoiceList(choices);
            }
        });

    }


}
