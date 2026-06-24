package com.achu.aijumptest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * projectName: com.achu.aijumptest.dto.ExamRecordDTO
 *
 * @author: achu_code
 * description: 考试记录数据传输对象
 */
public class ExamRecordDTO {

    @Data
    @Schema(description = "新增考试记录数据传输对象")
    public static class startExam {
        @Schema(description = "试卷id")
        private Integer paperId;
        @Schema(description = "学生姓名")
        private String studentName;
    }

    @Data
    @Schema(description = "提交试卷请求对象")
    public static class SubmitPaper {

        /**
         * 考生答案。
         *
         * key：题目ID
         * value：考生答案
         * - 单选/判断：字符串，例如 "A"、"TRUE"
         * - 多选：前端可以传数组，也可以传 "A,C"，后端会统一处理
         * - 简答：字符串
         */
        @Schema(description = "考生答案Map，key为题目ID，value为答案")
        private Map<Long, Object> answers;

        /**
         * 考试过程中切屏次数。
         *
         * 前端可以在考试页监听 visibilitychange / blur 事件，提交时一并传回。
         */
        @Schema(description = "窗口切换次数")
        private Integer windowSwitches;
    }

    @Data
    @Schema(description = "考试记录查询条件")
    public static class Query {

        @Schema(description = "试卷ID")
        private Integer paperId;

        @Schema(description = "考生姓名，模糊查询")
        private String studentName;

        @Schema(description = "考试状态：进行中、已批阅")
        private String status;
    }
}
