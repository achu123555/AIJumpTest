package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.QuestionDTO;
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
     *
     * 这个方法只负责把业务参数组装成一段稳定的 prompt，
     * 后续可以把返回的字符串作为 AI 对话补全接口的 user content。
     *
     * @param generateDTO AI 生成题目请求参数
     * @return 完整提示词
     */
    String buildAiQuestionPrompt(QuestionDTO.AiGenerate generateDTO);

    /**
     * 调用AI生成题目
     *
     * @param generateDTO
     * @return
     */
    List<QuestionDetailVO> callAiGenerateQuestion(QuestionDTO.AiGenerate generateDTO);
}
