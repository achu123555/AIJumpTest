package com.achu.aijumptest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * projectName: com.achu.aijumptest.dto.ExamRecordDTO
 *
 * @author: achu_code
 * description: 考试记录数据传输对象
 */

public class ExamRecordDTO {

    @Data
    @Schema(description = "新增考试记录数据传输对象")
    public static class startExam{
        @Schema(description = "试卷id")
        private Integer paperId;
        @Schema(description = "学生姓名")
        private String studentName;
    }
}
