package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.common.CacheConstants;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.PaperQuestion;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.entity.QuestionAnswer;
import com.achu.aijumptest.entity.QuestionChoice;
import com.achu.aijumptest.enums.QuestionType;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.excel.QuestionExportExcel;
import com.achu.aijumptest.excel.QuestionImportExcel;
import com.achu.aijumptest.listener.QuestionListener;
import com.achu.aijumptest.mapper.PaperQuestionMapper;
import com.achu.aijumptest.mapper.QuestionAnswerMapper;
import com.achu.aijumptest.mapper.QuestionChoiceMapper;
import com.achu.aijumptest.mapper.QuestionMapper;
import com.achu.aijumptest.service.QuestionService;
import com.achu.aijumptest.utils.ExcelUtils;
import com.achu.aijumptest.utils.RedisUtils;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
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
    private final PaperQuestionMapper paperQuestionMapper;

    @Override
    public Page<QuestionDetailVO> queryByPage(QuestionDTO.Query queryDTO) {
        Page<QuestionDetailVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        questionMapper.selectByPage(page, queryDTO);
        return page;
    }

    @Override
    public Page<QuestionDetailVO> queryByPageEnhance(QuestionDTO.Query queryDTO) {
        // 1. 分页 + 条件查询所有题目
        Page<Question> pageBean = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<Question> queryWrapper = buildQuestionQueryWrapper(queryDTO);
        queryWrapper.orderByDesc(Question::getCreateTime);

        page(pageBean, queryWrapper);

        // 2. entity 转成 VO
        List<QuestionDetailVO> voList = BeanUtil.copyToList(pageBean.getRecords(), QuestionDetailVO.class);
        Page<QuestionDetailVO> resultPage = new Page<>(pageBean.getCurrent(), pageBean.getSize(), pageBean.getTotal());
        resultPage.setRecords(voList);

        // 3. 批量填充答案和选项字段
        fillAnswerAndChoice(resultPage.getRecords());
        return resultPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void save(QuestionDTO.SaveAndUpdate saveAndUpdateDTO) {
        // 多表 DML 操作，需要开启事务。

        // 1. 先保存题目，再获取回显的 id
        Question question = BeanUtil.copyProperties(saveAndUpdateDTO, Question.class);
        baseMapper.insert(question);

        Long questionId = question.getId();
        if (questionId == null) {
            throw new BusinessException("题目保存失败，未获取到主键 ID！");
        }

        // 2. 判断题型：简答题和判断题直接保存答案；选择题需要保存选项并拼接答案
        QuestionAnswer answer = saveAndUpdateDTO.getQuestionAnswer();
        if (ObjectUtils.isNotNull(answer)) {
            answer.setQuestionId(questionId);
            questionAnswerMapper.insert(answer);
        } else {
            List<QuestionChoice> choices = saveAndUpdateDTO.getQuestionChoiceList();
            if (ObjectUtils.isNull(choices) || ObjectUtils.isEmpty(choices)) {
                throw new BusinessException("非法数据，选择题必须提供选项列表！");
            }

            // 3. 根据选项 isCorrect 字段拼接选择题答案，例如 A 或 A,C,D
            StringJoiner sj = new StringJoiner(",");
            for (QuestionChoice choice : choices) {
                choice.setQuestionId(questionId);
                if (Boolean.TRUE.equals(choice.getIsCorrect())) {
                    if (choice.getSort() == null) {
                        throw new BusinessException("非法数据：选项的序号 sort 不能为空！");
                    }
                    int sort = choice.getSort();
                    sj.add(String.valueOf((char) (sort + 'A' - 1)));
                }
            }
            if (sj.length() == 0) {
                throw new BusinessException("非法数据：选择题必须至少指定一个正确答案！");
            }

            // 4. 保存选择题答案
            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setQuestionId(questionId);
            questionAnswer.setAnswer(sj.toString());
            questionAnswerMapper.insert(questionAnswer);

            // 5. 保存选项。这里逐条 insert，兼容所有 MyBatis-Plus 版本。
            for (QuestionChoice choice : choices) {
                questionChoiceMapper.insert(choice);
            }
        }
    }

    @Override
    public QuestionDetailVO getById(Long id) {
        // 1. 查出题目
        Question question = baseMapper.selectById(id);
        if (ObjectUtils.isNull(question)) {
            throw new BusinessException("没有找到对应的题目！");
        }

        // 2. 拷贝题目，题目必有答案，直接查出答案填充
        QuestionDetailVO questionDetailVO = BeanUtil.copyProperties(question, QuestionDetailVO.class);
        QuestionAnswer answer = questionAnswerMapper.selectOne(
                new LambdaQueryWrapper<QuestionAnswer>()
                        .eq(QuestionAnswer::getQuestionId, questionDetailVO.getId())
                        .last("LIMIT 1")
        );
        questionDetailVO.setQuestionAnswer(answer);

        // 3. 只有选择题有选项
        if (QuestionType.CHOICE.name().equals(questionDetailVO.getType())) {
            List<QuestionChoice> choices = questionChoiceMapper.selectList(
                    new LambdaQueryWrapper<QuestionChoice>()
                            .eq(QuestionChoice::getQuestionId, questionDetailVO.getId())
                            .orderByAsc(QuestionChoice::getSort)
            );
            questionDetailVO.setQuestionChoiceList(choices);
        }

        // 4. 记录题目热度 ZINCRBY aijumptext:question:popular 1 题目ID
        redisUtils.zIncrementScore(CacheConstants.POPULAR_QUESTIONS_KEY, id, 1D);
        return questionDetailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(QuestionDTO.SaveAndUpdate updateDTO) {
        // 1. 先判断题目是否重复（不同 id 同名题目 == 重复）
        boolean exists = baseMapper.exists(
                new LambdaQueryWrapper<Question>()
                        .ne(Question::getId, updateDTO.getId())
                        .eq(Question::getTitle, updateDTO.getTitle())
        );
        if (exists) {
            throw new BusinessException("题目已存在，请勿重复提交！");
        }

        // 2. 更新题目主表
        Question question = BeanUtil.copyProperties(updateDTO, Question.class);
        int update = baseMapper.updateById(question);
        if (update == 0) {
            throw new BusinessException("更新失败，没有找到该题目！");
        }

        // 3. 更新简答题 / 判断题答案
        QuestionAnswer answer = updateDTO.getQuestionAnswer();
        if (ObjectUtils.isNotNull(answer)) {
            questionAnswerMapper.updateById(answer);
            return;
        }

        // 4. 更新选择题：重新拼接答案，并删除旧选项后插入新选项
        StringJoiner sj = new StringJoiner(",");
        List<QuestionChoice> choices = updateDTO.getQuestionChoiceList();
        for (QuestionChoice choice : choices) {
            choice.setQuestionId(question.getId());
            if (Boolean.TRUE.equals(choice.getIsCorrect())) {
                int sort = choice.getSort();
                sj.add(String.valueOf((char) (sort + 'A' - 1)));
            }
        }
        if (sj.length() == 0) {
            throw new BusinessException("请设置一个或多个正确选项！");
        }

        questionAnswerMapper.update(
                null,
                new LambdaUpdateWrapper<QuestionAnswer>()
                        .eq(QuestionAnswer::getQuestionId, question.getId())
                        .set(QuestionAnswer::getAnswer, sj.toString())
        );

        questionChoiceMapper.delete(
                new LambdaQueryWrapper<QuestionChoice>()
                        .eq(QuestionChoice::getQuestionId, question.getId())
        );
        for (QuestionChoice choice : choices) {
            questionChoiceMapper.insert(choice);
        }
    }

    @Override
    public List<QuestionDetailVO> getPopularQuestion(Integer size) {
        // 1. 从 redis 中查出 size 条分值最高的 id
        Set<Object> idsSet = redisUtils.zReverseRange(
                CacheConstants.POPULAR_QUESTIONS_KEY,
                0,
                size - 1
        );

        // 2. Redis 为空时，从数据库保底拉取最新的 size 条
        if (idsSet == null || idsSet.isEmpty()) {
            List<Question> questions = baseMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .orderByDesc(Question::getCreateTime)
                            .last("LIMIT " + size)
            );
            List<QuestionDetailVO> voList = BeanUtil.copyToList(questions, QuestionDetailVO.class);
            fillAnswerAndChoice(voList);
            return voList;
        }

        // 3. 将 id 的 Set<Object> 转成 List<Long>，再批量查出热门题目
        List<Long> idsList = idsSet.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        List<Question> redisQuestions = baseMapper.selectBatchIds(idsList);

        // 4. MySQL 查询结果可能乱序，这里按 Redis 热度顺序重新排序
        Map<Long, Question> questionMap = redisQuestions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        List<Question> sortQuestions = new ArrayList<>();
        for (Long id : idsList) {
            Question q = questionMap.get(id);
            if (q != null) {
                sortQuestions.add(q);
            }
        }
        List<QuestionDetailVO> popularQuestions = new ArrayList<>(BeanUtil.copyToList(sortQuestions, QuestionDetailVO.class));

        // 5. Redis 热门题不足 size 条时，再从数据库补齐最新题目
        int diff = size - idsSet.size();
        if (diff > 0) {
            List<Question> questions = baseMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .notIn(Question::getId, idsList)
                            .orderByDesc(Question::getCreateTime)
                            .last("LIMIT " + diff)
            );
            popularQuestions.addAll(BeanUtil.copyToList(questions, QuestionDetailVO.class));
        }

        // 6. 统一填充答案和选项
        fillAnswerAndChoice(popularQuestions);
        return popularQuestions;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        if (ObjectUtils.isNull(id)) {
            throw new BusinessException("非法参数：题目 id 不能为空");
        }

        // 1. 如果题目已经关联试卷，则不可删除
        boolean exists = paperQuestionMapper.exists(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getQuestionId, id)
        );
        if (exists) {
            throw new BusinessException("该题目已经关联了试卷，删除失败！");
        }

        // 2. 无关联时，先删子表，再删主表
        questionChoiceMapper.delete(
                new LambdaQueryWrapper<QuestionChoice>()
                        .eq(QuestionChoice::getQuestionId, id)
        );
        questionAnswerMapper.delete(
                new LambdaQueryWrapper<QuestionAnswer>()
                        .eq(QuestionAnswer::getQuestionId, id)
        );
        baseMapper.deleteById(id);

        // 3. 同步清理 Redis 热门题目缓存
        redisUtils.zRemove(CacheConstants.POPULAR_QUESTIONS_KEY, id);
    }

    /**
     * 批量导入题目 Excel。
     *
     * @param file 前端上传的 Excel 文件
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importQuestionExcel(MultipartFile file) {
        // Listener 不是 Spring Bean，所以这里 new 的时候要把当前 Service 传进去。
        ExcelUtils.readExcel(file, QuestionImportExcel.class, new QuestionListener(this));
    }

    /**
     * 保存一批 Excel 解析出来的题目数据。
     *
     * @param excelList Excel 行数据集合
     */
    @Override
    public void saveBatchFromExcel(List<QuestionImportExcel> excelList) {
        if (excelList == null || excelList.isEmpty()) {
            return;
        }

        // 1. 校验同一个 Excel 批次内是否有重复题目
        Set<String> titleSet = new HashSet<>();
        for (QuestionImportExcel excel : excelList) {
            String title = trim(excel.getTitle());
            if (!titleSet.add(title)) {
                throw new BusinessException("Excel 中存在重复题目：" + title);
            }
        }

        // 2. 逐行保存。每一行会落到 questions、question_answer、question_choices 三张表。
        for (QuestionImportExcel excel : excelList) {
            saveOneFromExcel(excel);
        }
    }

    /**
     * 根据查询条件导出题目 Excel。
     *
     * @param queryDTO 查询条件
     * @return Excel 导出行数据
     */
    @Override
    public List<QuestionExportExcel> listQuestionExport(QuestionDTO.Query queryDTO) {
        // 1. 导出时一般不走分页，只按查询条件筛选所有匹配题目
        LambdaQueryWrapper<Question> queryWrapper = buildQuestionQueryWrapper(queryDTO);
        queryWrapper.orderByDesc(Question::getCreateTime);

        List<Question> questionList = baseMapper.selectList(queryWrapper);
        if (questionList == null || questionList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 转 VO 后复用已有方法批量填充答案和选项
        List<QuestionDetailVO> voList = BeanUtil.copyToList(questionList, QuestionDetailVO.class);
        fillAnswerAndChoice(voList);

        // 3. 把 VO 转成扁平 Excel 行对象
        return voList.stream()
                .map(this::convertToExportExcel)
                .toList();
    }

    /**
     * 从 Excel 的一行数据保存一个完整题目。
     *
     * @param excel Excel 行数据
     */
    private void saveOneFromExcel(QuestionImportExcel excel) {
        // 1. 基础字段校验和格式标准化
        String title = trimRequired(excel.getTitle(), "题目不能为空");
        String type = parseQuestionType(excel.getType());
        String difficulty = parseDifficulty(excel.getDifficulty());
        Long categoryId = excel.getCategoryId();
        Integer score = excel.getScore();
        String answerText = trimRequired(excel.getAnswer(), "正确答案不能为空");

        if (categoryId == null) {
            throw new BusinessException("题目【" + title + "】的分类ID不能为空");
        }
        if (score == null || score <= 0) {
            throw new BusinessException("题目【" + title + "】的分值必须大于 0");
        }

        // 2. 数据库中不允许重复题目
        boolean exists = baseMapper.exists(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getTitle, title)
        );
        if (exists) {
            throw new BusinessException("题目已存在，不能重复导入：" + title);
        }

        // 3. 保存题目主表 questions
        Question question = new Question();
        question.setTitle(title);
        question.setType(type);
        question.setCategoryId(categoryId);
        question.setDifficulty(difficulty);
        question.setScore(score);
        question.setAnalysis(trim(excel.getAnalysis()));

        // 选择题才需要 multi；判断题、简答题固定 false
        boolean multi = QuestionType.CHOICE.name().equals(type) && parseBoolean(excel.getMulti());
        question.setMulti(multi);
        baseMapper.insert(question);

        Long questionId = question.getId();
        if (questionId == null) {
            throw new BusinessException("题目【" + title + "】保存失败，未获取到主键 ID");
        }

        // 4. 保存答案表 question_answer
        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestionId(questionId);
        questionAnswer.setKeywords(trim(excel.getKeywords()));

        // 5. 根据题型分别处理答案和选项
        if (QuestionType.CHOICE.name().equals(type)) {
            // 5.1 选择题：标准化正确答案，例如 A,C,D
            List<String> answerLetters = parseChoiceAnswer(answerText);
            if (answerLetters.size() > 1) {
                // 如果正确答案超过 1 个，自动认为是多选题，避免用户漏填“是否多选”。
                question.setMulti(true);
                baseMapper.updateById(question);
            }
            questionAnswer.setAnswer(String.join(",", answerLetters));
            questionAnswerMapper.insert(questionAnswer);

            // 5.2 选择题：保存选项表 question_choices
            List<QuestionChoice> choices = buildChoiceList(excel, questionId, answerLetters);
            for (QuestionChoice choice : choices) {
                questionChoiceMapper.insert(choice);
            }
        } else if (QuestionType.JUDGE.name().equals(type)) {
            // 5.3 判断题：把 正确/错误、对/错、true/false 统一转成 TRUE / FALSE
            questionAnswer.setAnswer(parseJudgeAnswer(answerText));
            questionAnswerMapper.insert(questionAnswer);
        } else {
            // 5.4 简答题：答案直接保存原文本
            questionAnswer.setAnswer(answerText);
            questionAnswerMapper.insert(questionAnswer);
        }
    }

    /**
     * 将题目 VO 转成 Excel 导出行对象。
     *
     * @param vo 题目展示 VO
     * @return Excel 导出对象
     */
    private QuestionExportExcel convertToExportExcel(QuestionDetailVO vo) {
        QuestionExportExcel excel = new QuestionExportExcel();
        excel.setId(vo.getId());
        excel.setTitle(vo.getTitle());
        excel.setType(vo.getType());
        excel.setMulti(vo.isMulti() ? "是" : "否");
        excel.setCategoryId(vo.getCategoryId());
        excel.setDifficulty(vo.getDifficulty());
        excel.setScore(vo.getScore());
        excel.setAnalysis(vo.getAnalysis());
        excel.setCreateTime(vo.getCreateTime());

        // 填充答案和关键词
        QuestionAnswer answer = vo.getQuestionAnswer();
        if (answer != null) {
            excel.setAnswer(answer.getAnswer());
            excel.setKeywords(answer.getKeywords());
        }

        // 填充选项 A-D
        List<QuestionChoice> choices = vo.getQuestionChoiceList();
        if (choices != null && !choices.isEmpty()) {
            choices.stream()
                    .sorted(Comparator.comparingInt(QuestionChoice::getSort))
                    .forEach(choice -> {
                        if (choice.getSort() == null) {
                            return;
                        }
                        switch (choice.getSort()) {
                            case 1 -> excel.setChoiceA(choice.getContent());
                            case 2 -> excel.setChoiceB(choice.getContent());
                            case 3 -> excel.setChoiceC(choice.getContent());
                            case 4 -> excel.setChoiceD(choice.getContent());
                            default -> {
                                // 当前模板只导出 A-D，超过 4 个选项时暂不处理。
                            }
                        }
                    });
        }
        return excel;
    }

    /**
     * 构建题目查询条件。
     * 分页查询和导出查询都可以复用这个方法。
     *
     * @param queryDTO 查询条件
     * @return MyBatis-Plus 查询包装器
     */
    private LambdaQueryWrapper<Question> buildQuestionQueryWrapper(QuestionDTO.Query queryDTO) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO == null) {
            return queryWrapper;
        }

        // 点击一级分类时，前端可传多个子分类 id
        if (queryDTO.getCategoryIds() != null && !queryDTO.getCategoryIds().isEmpty()) {
            queryWrapper.in(Question::getCategoryId, queryDTO.getCategoryIds());
        } else if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Question::getCategoryId, queryDTO.getCategoryId());
        }

        queryWrapper.eq(!ObjectUtils.isEmpty(queryDTO.getType()), Question::getType, queryDTO.getType());
        queryWrapper.eq(!ObjectUtils.isEmpty(queryDTO.getDifficulty()), Question::getDifficulty, queryDTO.getDifficulty());
        queryWrapper.like(!ObjectUtils.isEmpty(queryDTO.getKeyword()), Question::getTitle, queryDTO.getKeyword());
        return queryWrapper;
    }

    /**
     * 根据题目 id 批量填充答案和选项。
     *
     * @param records 题目 VO 集合
     */
    private void fillAnswerAndChoice(List<QuestionDetailVO> records) {
        // 1. 判空
        if (records == null || records.isEmpty()) {
            log.debug("问题集合为空");
            return;
        }

        // 2. 根据题目 id 查询所有答案和选项
        List<Long> ids = records.stream()
                .map(QuestionDetailVO::getId)
                .toList();
        List<QuestionAnswer> questionAnswers = questionAnswerMapper.selectList(
                new LambdaQueryWrapper<QuestionAnswer>()
                        .in(QuestionAnswer::getQuestionId, ids)
        );
        List<QuestionChoice> questionChoices = questionChoiceMapper.selectList(
                new LambdaQueryWrapper<QuestionChoice>()
                        .in(QuestionChoice::getQuestionId, ids)
                        .orderByAsc(QuestionChoice::getSort)
        );

        // 3. 答案和选项转成 Map，方便 O(1) 填充
        Map<Long, QuestionAnswer> answerMap = questionAnswers.stream()
                .collect(Collectors.toMap(
                        QuestionAnswer::getQuestionId,
                        questionAnswer -> questionAnswer,
                        (oldValue, newValue) -> oldValue
                ));
        Map<Long, List<QuestionChoice>> choiceMap = questionChoices.stream()
                .collect(Collectors.groupingBy(QuestionChoice::getQuestionId));

        // 4. 循环赋值
        records.forEach(questionPageVO -> {
            questionPageVO.setQuestionAnswer(
                    answerMap.getOrDefault(questionPageVO.getId(), new QuestionAnswer())
            );
            if (QuestionType.CHOICE.name().equals(questionPageVO.getType())) {
                List<QuestionChoice> choices = choiceMap.getOrDefault(questionPageVO.getId(), new ArrayList<>());
                choices.sort(Comparator.comparingInt(QuestionChoice::getSort));
                questionPageVO.setQuestionChoiceList(choices);
            }
        });
    }

    /**
     * 构建选择题选项列表。
     *
     * @param excel         Excel 行数据
     * @param questionId    题目 id
     * @param answerLetters 正确答案字母集合
     * @return 选项集合
     */
    private List<QuestionChoice> buildChoiceList(QuestionImportExcel excel, Long questionId, List<String> answerLetters) {
        List<QuestionChoice> result = new ArrayList<>();
        String[] contents = {
                trim(excel.getChoiceA()),
                trim(excel.getChoiceB()),
                trim(excel.getChoiceC()),
                trim(excel.getChoiceD())
        };

        for (int i = 0; i < contents.length; i++) {
            String content = contents[i];
            if (isBlank(content)) {
                continue;
            }
            String letter = String.valueOf((char) ('A' + i));

            QuestionChoice choice = new QuestionChoice();
            choice.setQuestionId(questionId);
            choice.setContent(content);
            choice.setSort(i + 1);
            choice.setIsCorrect(answerLetters.contains(letter));
            result.add(choice);
        }

        if (result.size() < 2) {
            throw new BusinessException("选择题至少需要填写两个选项");
        }

        // 校验正确答案对应的选项是否存在，例如答案为 D，但是选项 D 为空时直接报错。
        Set<Integer> existSortSet = result.stream()
                .map(QuestionChoice::getSort)
                .collect(Collectors.toSet());
        for (String letter : answerLetters) {
            int sort = letter.charAt(0) - 'A' + 1;
            if (!existSortSet.contains(sort)) {
                throw new BusinessException("正确答案 " + letter + " 对应的选项内容不能为空");
            }
        }
        return result;
    }

    /**
     * 解析题目类型。
     *
     * @param type Excel 中填写的题目类型
     * @return 标准题目类型：CHOICE / JUDGE / TEXT
     */
    private String parseQuestionType(String type) {
        String value = trimRequired(type, "题目类型不能为空").toUpperCase();
        return switch (value) {
            case "CHOICE", "选择题", "单选题", "多选题" -> QuestionType.CHOICE.name();
            case "JUDGE", "判断题", "是非题" -> QuestionType.JUDGE.name();
            case "TEXT", "简答题", "问答题" -> QuestionType.TEXT.name();
            default -> throw new BusinessException("题目类型填写错误，只能填写 CHOICE/JUDGE/TEXT 或 选择题/判断题/简答题");
        };
    }

    /**
     * 解析难度。
     *
     * @param difficulty Excel 中填写的难度
     * @return 标准难度：EASY / MEDIUM / HARD
     */
    private String parseDifficulty(String difficulty) {
        String value = trimRequired(difficulty, "难度不能为空").toUpperCase();
        return switch (value) {
            case "EASY", "简单", "容易" -> "EASY";
            case "MEDIUM", "中等", "普通" -> "MEDIUM";
            case "HARD", "困难", "较难" -> "HARD";
            default -> throw new BusinessException("难度填写错误，只能填写 EASY/MEDIUM/HARD 或 简单/中等/困难");
        };
    }

    /**
     * 解析布尔值。
     *
     * @param value Excel 中填写的布尔值
     * @return true / false
     */
    private boolean parseBoolean(String value) {
        if (isBlank(value)) {
            return false;
        }
        String v = value.trim().toUpperCase();
        return "TRUE".equals(v)
                || "1".equals(v)
                || "YES".equals(v)
                || "Y".equals(v)
                || "是".equals(v)
                || "多选".equals(v);
    }

    /**
     * 解析选择题答案。
     * 支持 A、A,C,D、ACD、A，C，D 等写法。
     *
     * @param answer Excel 中填写的答案
     * @return 正确答案字母集合
     */
    private List<String> parseChoiceAnswer(String answer) {
        String value = trimRequired(answer, "选择题正确答案不能为空")
                .replace("，", ",")
                .replace("、", ",")
                .replace(" ", "")
                .toUpperCase();

        LinkedHashSet<String> letters = new LinkedHashSet<>();
        for (char c : value.toCharArray()) {
            if (c == ',') {
                continue;
            }
            if (c < 'A' || c > 'D') {
                throw new BusinessException("选择题答案只能填写 A、B、C、D，例如 A 或 A,C,D");
            }
            letters.add(String.valueOf(c));
        }

        if (letters.isEmpty()) {
            throw new BusinessException("选择题正确答案不能为空");
        }
        return new ArrayList<>(letters);
    }

    /**
     * 解析判断题答案。
     *
     * @param answer Excel 中填写的判断题答案
     * @return TRUE / FALSE
     */
    private String parseJudgeAnswer(String answer) {
        String value = trimRequired(answer, "判断题答案不能为空").toUpperCase();
        return switch (value) {
            case "TRUE", "1", "YES", "Y", "正确", "对", "是" -> "TRUE";
            case "FALSE", "0", "NO", "N", "错误", "错", "否" -> "FALSE";
            default -> throw new BusinessException("判断题答案只能填写 TRUE/FALSE、正确/错误、对/错");
        };
    }

    /**
     * 字符串去除首尾空格，空字符串转成 null。
     *
     * @param value 原字符串
     * @return 处理后的字符串
     */
    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * 必填字符串处理。
     *
     * @param value        原字符串
     * @param errorMessage 为空时的错误提示
     * @return 去除首尾空格后的字符串
     */
    private String trimRequired(String value, String errorMessage) {
        String result = trim(value);
        if (result == null) {
            throw new BusinessException(errorMessage);
        }
        return result;
    }

    /**
     * 判断字符串是否为空。
     *
     * @param value 原字符串
     * @return 是否为空
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
