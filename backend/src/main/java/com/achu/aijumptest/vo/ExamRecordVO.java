package com.achu.aijumptest.vo;

import com.achu.aijumptest.entity.AnswerRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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
    public static class Detail extends BaseVO {

        @Schema(description = "试卷ID。关联的考试试卷", example = "1")
        private Integer paperId;

        @Schema(description = "考生姓名", example = "张三")
        private String studentName;

        @Schema(description = "考试得分", example = "85")
        private Integer score;

        @Schema(description = "AI评价记录")
        private String appraisal;

        @Schema(description = "考试开始时间", example = "2024-01-15 09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime startTime;

        @Schema(description = "考试结束时间", example = "2024-01-15 11:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime endTime;

        @Schema(description = "考试状态", example = "已批阅", allowableValues = {"进行中", "已批阅"})
        private String status;

        @Schema(description = "窗口切换次数")
        private Integer windowSwitches;

        @Schema(description = "详细的答题记录列表，包含每题的答案和得分情况")
        private List<AnswerRecord> answerRecords;

        @Schema(description = "关联的试卷信息，包含试卷详细内容和关联题目")
        private PaperVO.Detail detailPaper;
    }

    @Data
    @Schema(description = "考试记录列表项")
    public static class RecordItem {
        private Integer id;
        private Integer paperId;
        private String paperName;
        private String studentName;
        private Integer score;
        private BigDecimal totalScore;
        private String status;
        private Integer windowSwitches;
        private String appraisal;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime endTime;
    }

    @Data
    @Schema(description = "考试排行榜列表项")
    public static class RankingItem {
        private Integer rank;
        private Integer examRecordId;
        private Integer paperId;
        private String paperName;
        private String studentName;
        private Integer score;
        private BigDecimal totalScore;
        private Integer durationSeconds;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime endTime;
    }
}
