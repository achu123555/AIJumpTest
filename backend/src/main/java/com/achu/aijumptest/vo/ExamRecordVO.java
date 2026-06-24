package com.achu.aijumptest.vo;

import com.achu.aijumptest.entity.AnswerRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.vo.ExamRecordVO
 *
 * @author: achu_code
 * description: 考试记录 view Object
 */

public class ExamRecordVO {

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Schema(description = "考试记录详情")
    public static class Detail extends BaseVO{

        @Schema(description = "试卷ID。关联的考试试卷", example = "1")
        private Integer paperId;

        @Schema(description = "考生姓名", example = "张三")
        private String studentName;

        @Schema(description = "考试得分", example = "85")
        private Integer score;

        @Schema(description = "AI评价记录，存储AI对该考试试卷的评价内容", example = "该考生在Java基础题上表现优异，但MySQL索引优化部分理解稍显欠缺，建议加强B+树原理的复习。")
        private String appraisal;

        @Schema(description = "考试开始时间", example = "2024-01-15 09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime startTime;

        @Schema(description = "考试结束时间", example = "2024-01-15 11:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime endTime;

        @Schema(description = "考试状态", example = "已批阅", allowableValues = {"进行中", "已完成", "已批阅"})
        private String status;

        @Schema(description = "窗口切换次数，用于监控考试过程中的异常行为", example = "2")
        private Integer windowSwitches;

        @Schema(description = "详细的答题记录列表，包含每题的答案和得分情况")
        private List<AnswerRecord> answerRecords; // 答案记录列表

        @Schema(description = "关联的试卷信息，包含试卷详细内容和关联题目")
        private PaperVO.Detail detailPaper; // 试卷信息
    }
}
