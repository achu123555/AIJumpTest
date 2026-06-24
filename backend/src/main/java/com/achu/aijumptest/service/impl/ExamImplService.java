package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.dto.ExamRecordDTO;
import com.achu.aijumptest.entity.AnswerRecord;
import com.achu.aijumptest.entity.ExamRecord;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.AnswerRecordMapper;
import com.achu.aijumptest.mapper.ExamRecordMapper;
import com.achu.aijumptest.mapper.PaperMapper;
import com.achu.aijumptest.service.AIService;
import com.achu.aijumptest.service.ExamService;
import com.achu.aijumptest.service.PaperService;
import com.achu.aijumptest.vo.AiJudgeVO;
import com.achu.aijumptest.vo.ExamRecordVO;
import com.achu.aijumptest.vo.PaperVO;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * projectName: com.achu.aijumptest.service.impl.ExamImplService
 *
 * @author: achu_code
 * description: 考试业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExamImplService extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {

    private final PaperMapper paperMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final PaperService paperService;
    private final AIService aiService;

    @Override
    public ExamRecord startExam(ExamRecordDTO.startExam startExam) {
        if (startExam == null || startExam.getPaperId() == null || isBlank(startExam.getStudentName())) {
            throw new BusinessException("试卷ID和考生姓名不能为空！");
        }

        // 1. 校验试卷合法性：只有已发布试卷才允许考生开始考试。
        Paper paper = paperMapper.selectById(startExam.getPaperId());
        if (paper == null || !"PUBLISHED".equals(paper.getStatus())) {
            throw new BusinessException("该试卷不存在或未正式发布，无法开始考试！");
        }

        // 2. 同一个学生同一张试卷如果有进行中的记录，直接返回，避免重复创建考试记录。
        ExamRecord examRecord = baseMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getPaperId, startExam.getPaperId())
                        .eq(ExamRecord::getStudentName, startExam.getStudentName().trim())
                        .eq(ExamRecord::getStatus, "进行中")
        );
        if (examRecord != null) {
            return examRecord;
        }

        // 3. 防御前端双击开始考试。同一个 JVM 内部，用学生名+试卷ID做轻量锁即可。
        String lockKey = (startExam.getStudentName().trim() + "_" + startExam.getPaperId()).intern();
        synchronized (lockKey) {
            ExamRecord doubleCheck = baseMapper.selectOne(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getPaperId, startExam.getPaperId())
                            .eq(ExamRecord::getStudentName, startExam.getStudentName().trim())
                            .eq(ExamRecord::getStatus, "进行中")
            );
            if (doubleCheck != null) {
                return doubleCheck;
            }

            // 4. 新增考试记录，考试开始时还没有得分和AI评语。
            examRecord = new ExamRecord();
            examRecord.setPaperId(startExam.getPaperId());
            examRecord.setStudentName(startExam.getStudentName().trim());
            examRecord.setStatus("进行中");
            examRecord.setScore(0);
            examRecord.setWindowSwitches(0);
            examRecord.setStartTime(LocalDateTime.now());
            baseMapper.insert(examRecord);
            return examRecord;
        }
    }

    @Override
    public ExamRecordVO.Detail getDetailRecordById(Integer id) {
        ExamRecord examRecord = baseMapper.selectById(id);
        if (examRecord == null) {
            throw new BusinessException("该考试记录不存在！");
        }

        // 查询该次考试的所有作答记录。未提交时返回空数组，前端可正常进入作答页。
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getExamRecordId, examRecord.getId())
                        .orderByAsc(AnswerRecord::getQuestionId)
        );
        if (ObjectUtils.isEmpty(answerRecords)) {
            answerRecords = new ArrayList<>();
        }

        PaperVO.Detail detailPaper = paperService.getDetailPaper(examRecord.getPaperId());
        ExamRecordVO.Detail detailRecord = BeanUtil.copyProperties(examRecord, ExamRecordVO.Detail.class);
        detailRecord.setAnswerRecords(answerRecords);
        detailRecord.setDetailPaper(detailPaper);
        return detailRecord;
    }

    @Override
    public List<Paper> listPublishedPapers(String name) {
        // 学生端只能看到已发布试卷，避免草稿卷被误考。
        return paperMapper.selectList(
                new LambdaQueryWrapper<Paper>()
                        .eq(Paper::getStatus, "PUBLISHED")
                        .like(!isBlank(name), Paper::getName, name)
                        .orderByDesc(Paper::getCreateTime)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ExamRecordVO.Detail submitPaper(Integer examRecordId, ExamRecordDTO.SubmitPaper submitPaper) {
        ExamRecord examRecord = baseMapper.selectById(examRecordId);
        if (examRecord == null) {
            throw new BusinessException("考试记录不存在，无法提交！");
        }
        if (!"进行中".equals(examRecord.getStatus())) {
            throw new BusinessException("该考试已经提交或批阅完成，请勿重复提交！");
        }

        PaperVO.Detail detailPaper = paperService.getDetailPaper(examRecord.getPaperId());
        List<QuestionDetailVO> questions = detailPaper.getQuestions();
        if (ObjectUtils.isEmpty(questions)) {
            throw new BusinessException("该试卷没有题目，无法提交！");
        }

        Map<Long, Object> answerMap = submitPaper == null || submitPaper.getAnswers() == null
                ? new HashMap<>()
                : submitPaper.getAnswers();

        // 防御重复提交时产生重复 answer_record。正常情况下状态会挡住，这里再清理一次更安全。
        answerRecordMapper.delete(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getExamRecordId, examRecordId)
        );

        int totalScore = 0;
        List<AnswerRecord> savedRecords = new ArrayList<>();

        for (QuestionDetailVO question : questions) {
            String userAnswer = stringifyAnswer(answerMap.get(question.getId()));
            int maxScore = getQuestionRealScore(question);

            AnswerRecord record = new AnswerRecord();
            record.setExamRecordId(examRecordId);
            record.setQuestionId(question.getId());
            record.setUserAnswer(userAnswer);

            if ("CHOICE".equals(question.getType())) {
                judgeChoice(question, userAnswer, maxScore, record);
            } else if ("JUDGE".equals(question.getType())) {
                judgeJudgeQuestion(question, userAnswer, maxScore, record);
            } else if ("TEXT".equals(question.getType())) {
                judgeText(question, userAnswer, maxScore, record);
            } else {
                record.setScore(0);
                record.setIsCorrect(0);
                record.setAiCorrection("未知题型，系统未判分，请管理员复核。");
            }

            totalScore += record.getScore() == null ? 0 : record.getScore();
            answerRecordMapper.insert(record);
            savedRecords.add(record);
        }

        // AI 总评失败不影响交卷，AIService 内部已经做了兜底。
        String appraisal = aiService.generateExamAppraisal(detailPaper, savedRecords, totalScore);

        examRecord.setScore(totalScore);
        examRecord.setStatus("已批阅");
        examRecord.setEndTime(LocalDateTime.now());
        examRecord.setAppraisal(appraisal);
        examRecord.setWindowSwitches(submitPaper == null || submitPaper.getWindowSwitches() == null
                ? examRecord.getWindowSwitches()
                : submitPaper.getWindowSwitches());
        baseMapper.updateById(examRecord);

        return getDetailRecordById(examRecordId);
    }

    @Override
    public List<ExamRecordVO.RecordItem> listExamRecords(ExamRecordDTO.Query query) {
        ExamRecordDTO.Query safeQuery = query == null ? new ExamRecordDTO.Query() : query;

        List<ExamRecord> records = baseMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(safeQuery.getPaperId() != null, ExamRecord::getPaperId, safeQuery.getPaperId())
                        .like(!isBlank(safeQuery.getStudentName()), ExamRecord::getStudentName, safeQuery.getStudentName())
                        .eq(!isBlank(safeQuery.getStatus()), ExamRecord::getStatus, safeQuery.getStatus())
                        .orderByDesc(ExamRecord::getCreateTime)
        );
        if (ObjectUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Map<Integer, Paper> paperMap = getPaperMap(records.stream().map(ExamRecord::getPaperId).collect(Collectors.toSet()));

        return records.stream().map(record -> {
            Paper paper = paperMap.get(record.getPaperId());
            ExamRecordVO.RecordItem item = new ExamRecordVO.RecordItem();
            item.setId(record.getId());
            item.setPaperId(record.getPaperId());
            item.setPaperName(paper == null ? "未知试卷" : paper.getName());
            item.setStudentName(record.getStudentName());
            item.setScore(record.getScore());
            item.setTotalScore(paper == null ? BigDecimal.ZERO : paper.getTotalScore());
            item.setStatus(record.getStatus());
            item.setWindowSwitches(record.getWindowSwitches());
            item.setAppraisal(record.getAppraisal());
            item.setStartTime(record.getStartTime());
            item.setEndTime(record.getEndTime());
            return item;
        }).toList();
    }

    @Override
    public List<ExamRecordVO.RankingItem> listRanking(Integer paperId) {
        List<ExamRecord> records = baseMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(paperId != null, ExamRecord::getPaperId, paperId)
                        .eq(ExamRecord::getStatus, "已批阅")
                        .orderByDesc(ExamRecord::getScore)
                        .orderByAsc(ExamRecord::getEndTime)
        );
        if (ObjectUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Map<Integer, Paper> paperMap = getPaperMap(records.stream().map(ExamRecord::getPaperId).collect(Collectors.toSet()));
        List<ExamRecordVO.RankingItem> result = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            ExamRecord record = records.get(i);
            Paper paper = paperMap.get(record.getPaperId());

            ExamRecordVO.RankingItem item = new ExamRecordVO.RankingItem();
            item.setRank(i + 1);
            item.setExamRecordId(record.getId());
            item.setPaperId(record.getPaperId());
            item.setPaperName(paper == null ? "未知试卷" : paper.getName());
            item.setStudentName(record.getStudentName());
            item.setScore(record.getScore());
            item.setTotalScore(paper == null ? BigDecimal.ZERO : paper.getTotalScore());
            item.setEndTime(record.getEndTime());

            if (record.getStartTime() != null && record.getEndTime() != null) {
                item.setDurationSeconds((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
            } else {
                item.setDurationSeconds(0);
            }

            result.add(item);
        }

        return result;
    }

    /**
     * 选择题判分。
     *
     * 多选题答案可能是 A,C，也可能前端传数组后被 stringify 成 A,C。
     * 后端统一排序后比较，避免 C,A 这种顺序问题被误判。
     */
    private void judgeChoice(QuestionDetailVO question, String userAnswer, int maxScore, AnswerRecord record) {
        String standardAnswer = question.getQuestionAnswer() == null ? "" : question.getQuestionAnswer().getAnswer();
        boolean correct = normalizeChoiceAnswer(userAnswer).equals(normalizeChoiceAnswer(standardAnswer));
        record.setScore(correct ? maxScore : 0);
        record.setIsCorrect(correct ? 1 : 0);
        record.setAiCorrection(correct ? "回答正确。" : "回答错误，正确答案为：" + standardAnswer);
    }

    /**
     * 判断题判分。
     *
     * 兼容 TRUE/FALSE、正确/错误、对/错、1/0。
     */
    private void judgeJudgeQuestion(QuestionDetailVO question, String userAnswer, int maxScore, AnswerRecord record) {
        String standardAnswer = question.getQuestionAnswer() == null ? "" : question.getQuestionAnswer().getAnswer();
        boolean correct = normalizeJudgeAnswer(userAnswer).equals(normalizeJudgeAnswer(standardAnswer));
        record.setScore(correct ? maxScore : 0);
        record.setIsCorrect(correct ? 1 : 0);
        record.setAiCorrection(correct ? "回答正确。" : "回答错误，正确答案为：" + normalizeJudgeAnswer(standardAnswer));
    }

    /**
     * 简答题判分：调用 AI 进行语义评分。
     */
    private void judgeText(QuestionDetailVO question, String userAnswer, int maxScore, AnswerRecord record) {
        if (isBlank(userAnswer)) {
            record.setScore(0);
            record.setIsCorrect(0);
            record.setAiCorrection("考生未作答，本题0分。");
            return;
        }

        AiJudgeVO.TextJudgeResult judgeResult = aiService.judgeTextAnswer(question, userAnswer, maxScore);
        int score = judgeResult.getScore() == null ? 0 : judgeResult.getScore();
        score = Math.max(0, Math.min(score, maxScore));
        record.setScore(score);
        record.setIsCorrect(score >= maxScore ? 1 : 0);
        record.setAiCorrection(judgeResult.getCorrection());
    }

    private int getQuestionRealScore(QuestionDetailVO question) {
        if (question.getRealScore() != null) {
            return question.getRealScore().intValue();
        }
        return question.getScore() == null ? 0 : question.getScore();
    }

    private String stringifyAnswer(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        return String.valueOf(value).trim();
    }

    private String normalizeChoiceAnswer(String answer) {
        if (isBlank(answer)) {
            return "";
        }
        return Arrays.stream(answer.toUpperCase()
                        .replace("，", ",")
                        .replace(" ", "")
                        .split(","))
                .filter(item -> !item.isBlank())
                .sorted()
                .collect(Collectors.joining(","));
    }

    private String normalizeJudgeAnswer(String answer) {
        if (isBlank(answer)) {
            return "";
        }
        String value = answer.trim().toUpperCase();
        return switch (value) {
            case "TRUE", "T", "1", "正确", "对", "是" -> "TRUE";
            case "FALSE", "F", "0", "错误", "错", "否" -> "FALSE";
            default -> value;
        };
    }

    private Map<Integer, Paper> getPaperMap(Set<Integer> paperIds) {
        if (ObjectUtils.isEmpty(paperIds)) {
            return new HashMap<>();
        }
        List<Paper> papers = paperMapper.selectBatchIds(paperIds);
        return papers.stream().collect(Collectors.toMap(Paper::getId, paper -> paper));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
