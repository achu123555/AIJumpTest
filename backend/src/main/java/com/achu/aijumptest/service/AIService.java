package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.AnswerRecord;
import com.achu.aijumptest.vo.AiJudgeVO;
import com.achu.aijumptest.vo.PaperVO;
import com.achu.aijumptest.vo.QuestionDetailVO;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.AIService
 *
 * @author: achu_code
 * description: 调用AI对话补全接口
 */
public interface AIService {

    /**
     * 构建 AI 生成题目的完整提示词。
     */
    String buildAiQuestionPrompt(QuestionDTO.AiGenerate generateDTO);

    /**
     * 调用AI生成题目。
     */
    List<QuestionDetailVO> callAiGenerateQuestion(QuestionDTO.AiGenerate generateDTO);

    /**
     * 调用 AI 批阅简答题。
     *
     * @param question   题目信息，包含标准答案、关键词、解析
     * @param userAnswer 考生答案
     * @param maxScore   本题满分，来自 paper_question.score
     * @return AI 批阅分数和评语
     */
    AiJudgeVO.TextJudgeResult judgeTextAnswer(QuestionDetailVO question, String userAnswer, Integer maxScore);

    /**
     * 调用 AI 生成整张试卷的综合评语。
     *
     * @param paper         试卷详情
     * @param answerRecords 本次考试每题得分记录
     * @param totalScore    本次考试总分
     * @return AI 总评
     */
    String generateExamAppraisal(PaperVO.Detail paper, List<AnswerRecord> answerRecords, Integer totalScore);
}
