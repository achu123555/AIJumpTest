package com.achu.aijumptest.service.impl;

import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.enums.QuestionType;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.properties.DeepSeekProperties;
import com.achu.aijumptest.service.AIService;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * projectName: com.achu.aijumptest.service.impl.AIServiceImpl
 *
 * @author: achu_code
 * description: 调用AI对话补全实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final WebClient webClient;
    private final DeepSeekProperties deepSeekProperties;

    @Override
    public String buildAiQuestionPrompt(QuestionDTO.AiGenerate generateDTO) {

        if (generateDTO == null) {
            throw new BusinessException("AI生成参数不能为空！");
        }

        // 题型默认值：选择题
        String type = normalizeQuestionType(generateDTO.getType());

        // 难度默认值：中等
        String difficulty = normalizeDifficulty(generateDTO.getDifficulty());

        // 是否多选，只有选择题有效
        boolean multi = Boolean.TRUE.equals(generateDTO.getMulti());

        // 分值默认 5 分
        Integer score = generateDTO.getScore() == null || generateDTO.getScore() <= 0
                ? 5
                : generateDTO.getScore();

        // 生成数量默认 5 条，最多限制 20 条，避免一次让 AI 生成过多导致结果不稳定
        Integer count = generateDTO.getCount() == null || generateDTO.getCount() <= 0
                ? 5
                : Math.min(generateDTO.getCount(), 20);

        String categoryName = isBlank(generateDTO.getCategoryName())
                ? "未指定分类"
                : generateDTO.getCategoryName().trim();

        String requirement = isBlank(generateDTO.getRequirement())
                ? "题目表述清晰，答案准确，解析简洁但有教学意义。"
                : generateDTO.getRequirement().trim();

        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的在线刷题系统题库出题助手。")
                .append("请根据下面的业务要求生成题目数据，返回结果用于 Java 后端直接解析入库。")
                .append("\n\n");

        prompt.append("【业务背景】\n")
                .append("系统名称：AIJumpTest 在线题库系统。\n")
                .append("题目主表：questions。\n")
                .append("题目答案表：question_answer。\n")
                .append("题目选项表：question_choices。\n");

        prompt.append("【本次生成参数】\n")
                .append("分类ID：").append(generateDTO.getCategoryId()).append("\n")
                .append("分类名称：").append(categoryName).append("\n")
                .append("题型：").append(type).append("\n")
                .append("是否多选：").append(multi).append("\n")
                .append("难度：").append(difficulty).append("\n")
                .append("默认分值：").append(score).append("\n")
                .append("生成数量：").append(count).append("\n")
                .append("额外要求：").append(requirement).append("\n\n");

        prompt.append("【字段说明】\n")
                .append("每道题必须包含以下字段：\n")
                .append("id：固定为 null。\n")
                .append("title：题目标题，不能为空。\n")
                .append("type：只能是 CHOICE、JUDGE、TEXT。\n")
                .append("categoryId：固定使用本次传入的分类ID。\n")
                .append("multi：是否多选，仅 CHOICE 有效。\n")
                .append("difficulty：只能是 EASY、MEDIUM、HARD。\n")
                .append("score：题目分值。\n")
                .append("analysis：答案解析，不能为空。\n")
                .append("questionAnswer：题目答案对象。\n")
                .append("questionChoiceList：选择题选项列表。\n\n");

        prompt.append("【题型规则】\n")
                .append("1. 如果 type 是 CHOICE：\n")
                .append("   - questionAnswer 必须返回 null。\n")
                .append("   - questionChoiceList 必须返回 4 个选项。\n")
                .append("   - 每个选项包含 content、isCorrect、sort。\n")
                .append("   - sort 从 1 到 4，对应 A、B、C、D。\n")
                .append("   - 单选题只能有 1 个 isCorrect=true。\n")
                .append("   - 多选题至少有 2 个 isCorrect=true。\n")
                .append("   - 不要在 content 里写 A.、B.、C.、D. 前缀。\n")
                .append("   - 后端会根据 isCorrect 和 sort 自动生成答案。\n\n");

        prompt.append("2. 如果 type 是 JUDGE：\n")
                .append("   - questionChoiceList 必须返回空数组 []。\n")
                .append("   - questionAnswer 不能为空。\n")
                .append("   - questionAnswer.answer 只能是 true 或 false。\n")
                .append("   - questionAnswer.keywords 可以填写 1 到 3 个关键词，用中文逗号分隔。\n\n");

        prompt.append("3. 如果 type 是 TEXT：\n")
                .append("   - questionChoiceList 必须返回空数组 []。\n")
                .append("   - questionAnswer 不能为空。\n")
                .append("   - questionAnswer.answer 填写参考答案。\n")
                .append("   - questionAnswer.keywords 填写评分关键词，用中文逗号分隔。\n\n");

        prompt.append("【输出格式要求】\n")
                .append("只允许返回 JSON 数组。\n")
                .append("不要返回 Markdown。\n")
                .append("不要返回 ```json 代码块。\n")
                .append("不要返回解释说明。\n")
                .append("不要返回多余文本。\n")
                .append("JSON 字段名必须使用双引号。\n")
                .append("字符串内容必须使用双引号。\n")
                .append("布尔值必须使用 true 或 false。\n\n");

        prompt.append("【返回 JSON 示例】\n");

        if (QuestionType.CHOICE.name().equals(type)) {
            prompt.append("[\n")
                    .append("  {\n")
                    .append("    \"id\": null,\n")
                    .append("    \"title\": \"以下关于 ArrayList 的说法正确的是？\",\n")
                    .append("    \"type\": \"CHOICE\",\n")
                    .append("    \"categoryId\": ").append(generateDTO.getCategoryId()).append(",\n")
                    .append("    \"multi\": false,\n")
                    .append("    \"difficulty\": \"").append(difficulty).append("\",\n")
                    .append("    \"score\": ").append(score).append(",\n")
                    .append("    \"analysis\": \"ArrayList 底层基于数组实现，适合随机访问，但中间插入和删除可能需要移动元素。\",\n")
                    .append("    \"questionAnswer\": null,\n")
                    .append("    \"questionChoiceList\": [\n")
                    .append("      {\"content\": \"底层基于数组实现，支持快速随机访问\", \"isCorrect\": true, \"sort\": 1},\n")
                    .append("      {\"content\": \"底层基于链表实现，随机访问效率最高\", \"isCorrect\": false, \"sort\": 2},\n")
                    .append("      {\"content\": \"只能存储基本数据类型\", \"isCorrect\": false, \"sort\": 3},\n")
                    .append("      {\"content\": \"线程安全，适合所有并发场景\", \"isCorrect\": false, \"sort\": 4}\n")
                    .append("    ]\n")
                    .append("  }\n")
                    .append("]\n");
        } else if (QuestionType.JUDGE.name().equals(type)) {
            prompt.append("[\n")
                    .append("  {\n")
                    .append("    \"id\": null,\n")
                    .append("    \"title\": \"HashMap 在 JDK 8 中发生哈希冲突时，链表过长可能会转换为红黑树。\",\n")
                    .append("    \"type\": \"JUDGE\",\n")
                    .append("    \"categoryId\": ").append(generateDTO.getCategoryId()).append(",\n")
                    .append("    \"multi\": false,\n")
                    .append("    \"difficulty\": \"").append(difficulty).append("\",\n")
                    .append("    \"score\": ").append(score).append(",\n")
                    .append("    \"analysis\": \"JDK 8 中 HashMap 在满足一定条件时会将过长链表转换为红黑树，以提升查询效率。\",\n")
                    .append("    \"questionAnswer\": {\"answer\": \"true\", \"keywords\": \"HashMap,JDK8,红黑树\"},\n")
                    .append("    \"questionChoiceList\": []\n")
                    .append("  }\n")
                    .append("]\n");
        } else {
            prompt.append("[\n")
                    .append("  {\n")
                    .append("    \"id\": null,\n")
                    .append("    \"title\": \"请简述 ArrayList 和 LinkedList 的主要区别。\",\n")
                    .append("    \"type\": \"TEXT\",\n")
                    .append("    \"categoryId\": ").append(generateDTO.getCategoryId()).append(",\n")
                    .append("    \"multi\": false,\n")
                    .append("    \"difficulty\": \"").append(difficulty).append("\",\n")
                    .append("    \"score\": ").append(score).append(",\n")
                    .append("    \"analysis\": \"本题考查 Java 集合中顺序表和链表结构的理解。\",\n")
                    .append("    \"questionAnswer\": {\"answer\": \"ArrayList 底层基于数组，随机访问快，插入删除可能需要移动元素；LinkedList 底层基于双向链表，插入删除较方便，但随机访问效率较低。\", \"keywords\": \"数组,链表,随机访问,插入删除\"},\n")
                    .append("    \"questionChoiceList\": []\n")
                    .append("  }\n")
                    .append("]\n");
        }

        prompt.append("\n请严格按照上面的 JSON 结构，生成 %s 道符合要求的题目的数组。".formatted(count));

        return prompt.toString();
    }

    @Override
    public List<QuestionDetailVO> callAiGenerateQuestion(QuestionDTO.AiGenerate generateDTO) {

        //1.封装官方文档要求的请求JSON
        String prompt = buildAiQuestionPrompt(generateDTO);

        Map<String,String> userMap = new HashMap<>();
        userMap.put("content",prompt);
        userMap.put("role","user");
        List<Map<String,String>> msgList = new ArrayList<>();
        msgList.add(userMap);

        Map<String,Object> requestBody = new HashMap<>();
        requestBody.put("messages",msgList);
        requestBody.put("model",deepSeekProperties.getModel());
        requestBody.put("max_tokens",deepSeekProperties.getMaxTokens());
        requestBody.put("temperature",deepSeekProperties.getTemperature());


        //2.配置mono发送网络请求
        Mono<String> mono = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                // 重点：捕获 4xx / 5xx，并读取 DeepSeek 返回的错误 body
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(errorBody -> {
                                    log.error("【AI服务】DeepSeek接口异常，状态码：{}，响应内容：{}",
                                            response.statusCode().value(),
                                            errorBody);

                                    int statusCode = response.statusCode().value();

                                    if (statusCode == 400) {
                                        return new BusinessException("AI请求参数错误：" + errorBody);
                                    }

                                    if (statusCode == 401) {
                                        return new BusinessException("AI认证失败，请检查 API Key！");
                                    }

                                    if (statusCode == 402) {
                                        return new BusinessException("AI账户余额不足或额度不足！");
                                    }

                                    if (statusCode == 429) {
                                        return new BusinessException("AI请求过于频繁，请稍后再试！");
                                    }

                                    if (statusCode >= 500) {
                                        return new BusinessException("AI服务暂时不可用，请稍后再试！");
                                    }

                                    return new BusinessException("AI调用失败：" + errorBody);
                                })
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30));

        //3.发起同步请求且解析结果(失败重试三次)
        String result = null;
        for (int i = 1; i <= 3; i++) {
            try {
                result = mono.block();
                if (ObjectUtils.isNotNull(result)) {
                    break; //成功拿到结果，退出循环！
                }
            }catch (BusinessException businessException){
                throw businessException;
            }catch (Exception e){
                if (i == 3) {
                    throw e;
                }
                log.warn("【AI服务】第 {} 次调用失败，正在发起下一次重试...", i);
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException ie){
                    Thread.currentThread().interrupt();
                }
            }
        }

        //4.开始对结果拆分
        JSONObject parseObject = JSONObject.parseObject(result);
        if (parseObject == null) {
            throw new BusinessException("AI服务返回结果解析失败！");
        }
        if (ObjectUtils.isNotNull(parseObject.getJSONObject("error"))) {
            throw new BusinessException("error");
        }
        JSONArray choices = parseObject.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException("大模型未返回有效的 choices 字段!");
        }

        //4.1 拆出content字段内容
        String jsonContent = parseObject.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
        if (isBlank(jsonContent)) {
            throw new BusinessException("大模型返回的内容为空！");
        }

        //4.2 字符串截取  ```json xxxx ```  格式数据
        String content = jsonContent.trim();
        if (jsonContent.contains("```json")) {
            int start = jsonContent.indexOf("```json");
            int end = jsonContent.lastIndexOf("```");
            if(start<end){
                content = content.substring(start+7, end).trim();
            }
        }

        //5 将截取出的json字符串反序列化为VO
        try {
            // Fastjson2 会根据字段名自动填充：title, type, difficulty, score, questionChoiceList
            return JSONArray.parseArray(content, QuestionDetailVO.class);

        }catch (Exception e){
            log.error("【AI服务】解析大模型生成的题目JSON时发生格式错误！原始文本: {}", content, e);
            throw new BusinessException("AI 生成的数据格式解析失败，请重新尝试生成！");
        }

    }


    /**
     * 规范化题型。
     */
    private String normalizeQuestionType(String type) {
        if (isBlank(type)) {
            return QuestionType.CHOICE.name();
        }

        String value = type.trim().toUpperCase();

        return switch (value) {
            case "CHOICE", "JUDGE", "TEXT" -> value;
            case "选择题", "单选题", "多选题" -> "CHOICE";
            case "判断题" -> "JUDGE";
            case "简答题", "问答题" -> "TEXT";
            default -> throw new BusinessException("不支持的题型：" + type);
        };
    }

    /**
     * 规范化难度。
     */
    private String normalizeDifficulty(String difficulty) {
        if (isBlank(difficulty)) {
            return "MEDIUM";
        }

        String value = difficulty.trim().toUpperCase();

        return switch (value) {
            case "EASY", "MEDIUM", "HARD" -> value;
            case "简单", "容易", "初级" -> "EASY";
            case "中等", "普通", "中级" -> "MEDIUM";
            case "困难", "高级", "难" -> "HARD";
            default -> throw new BusinessException("不支持的难度：" + difficulty);
        };
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
