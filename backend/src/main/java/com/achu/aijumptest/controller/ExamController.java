package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.dto.ExamRecordDTO;
import com.achu.aijumptest.entity.ExamRecord;
import com.achu.aijumptest.service.ExamService;
import com.achu.aijumptest.vo.ExamRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * projectName: com.achu.aijumptest.controller.ExamController
 *
 * @author: achu_code
 * description: 考试控制层
 */

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @Operation(
            summary = "新增考试记录接口",
            description = "点击开始考试,传入试卷id和考生姓名新增考试记录,已存在且进行中状态则返回"
    )
    public Result<ExamRecord> startExam(
            @Parameter(description = "试卷id和考生姓名") @RequestBody ExamRecordDTO.startExam startExam
            ){
        //1.创建examRecord对象
        log.info("考生：{} 要求开始考试，试卷id：{}",
                startExam.getStudentName(),startExam.getPaperId());
        //2.返回已入库examRecord对象
        ExamRecord examRecorded = examService.startExam(startExam);
        return Result.success(examRecorded);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询考试记录详情接口",description = "此考试记录包括基本信息及作答记录和试卷和题目")
    public Result<ExamRecordVO.Detail> getDetailRecordById(
            @Parameter(description = "考试记录id") @PathVariable("id") Integer id
    ){

        ExamRecordVO.Detail detailRecord = examService.getDetailRecordById(id);

        return null;
    }
}
