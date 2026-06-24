package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.dto.ExamRecordDTO;
import com.achu.aijumptest.entity.ExamRecord;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.service.ExamService;
import com.achu.aijumptest.vo.ExamRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/papers")
    @Operation(summary = "学生端查询可考试卷接口", description = "只返回已发布试卷，支持按试卷名称模糊搜索")
    public Result<List<Paper>> listPublishedPapers(
            @Parameter(description = "试卷名称") @RequestParam(name = "name", required = false) String name
    ) {
        log.info("学生端查询可考试卷列表，name={}", name);
        return Result.success(examService.listPublishedPapers(name));
    }

    @PostMapping
    @Operation(
            summary = "新增考试记录接口",
            description = "点击开始考试，传入试卷id和考生姓名新增考试记录；如果已经存在进行中记录，则直接返回原记录"
    )
    public Result<ExamRecord> startExam(
            @Parameter(description = "试卷id和考生姓名") @RequestBody ExamRecordDTO.startExam startExam
    ) {
        log.info("考生：{} 要求开始考试，试卷id：{}", startExam.getStudentName(), startExam.getPaperId());
        ExamRecord examRecorded = examService.startExam(startExam);
        return Result.success(examRecorded);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询考试记录详情接口", description = "包括考试记录、作答记录、试卷、题目、选项")
    public Result<ExamRecordVO.Detail> getDetailRecordById(
            @Parameter(description = "考试记录id") @PathVariable("id") Integer id
    ) {
        log.info("要查询详情的考试记录id为：{}", id);
        ExamRecordVO.Detail detailRecord = examService.getDetailRecordById(id);
        return Result.success(detailRecord);
    }

    @PostMapping("/{examRecordId}/submit")
    @Operation(summary = "提交试卷接口", description = "保存考生作答，自动判选择/判断题，调用AI批阅简答题并生成试卷评语")
    public Result<ExamRecordVO.Detail> submitPaper(
            @Parameter(description = "考试记录id") @PathVariable("examRecordId") Integer examRecordId,
            @RequestBody ExamRecordDTO.SubmitPaper submitPaper
    ) {
        log.info("考试记录 {} 开始提交试卷", examRecordId);
        ExamRecordVO.Detail result = examService.submitPaper(examRecordId, submitPaper);
        return Result.success(result);
    }

    @GetMapping("/records")
    @Operation(summary = "后台查询考试记录列表接口")
    public Result<List<ExamRecordVO.RecordItem>> listExamRecords(ExamRecordDTO.Query query) {
        log.info("后台查询考试记录列表，参数：{}", query);
        return Result.success(examService.listExamRecords(query));
    }

    @GetMapping("/ranking")
    @Operation(summary = "排行榜接口", description = "paperId为空时查询全部已批阅记录；paperId不为空时查询某张试卷排行榜")
    public Result<List<ExamRecordVO.RankingItem>> listRanking(
            @Parameter(description = "试卷ID") @RequestParam(name = "paperId", required = false) Integer paperId
    ) {
        log.info("查询考试排行榜，paperId={}", paperId);
        return Result.success(examService.listRanking(paperId));
    }
}
