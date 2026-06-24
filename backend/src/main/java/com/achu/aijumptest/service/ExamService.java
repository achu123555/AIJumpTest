package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.ExamRecordDTO;
import com.achu.aijumptest.entity.ExamRecord;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.vo.ExamRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.ExamService
 *
 * @author: achu_code
 * description: 考试业务层接口
 */
public interface ExamService extends IService<ExamRecord> {

    /**
     * 开始考试。
     * - 新增考试记录
     * - 已存在进行中的考试记录时，直接返回原记录，避免重复开考
     */
    ExamRecord startExam(ExamRecordDTO.startExam startExam);

    /**
     * 获取考试记录详情。
     * 包括考试记录、作答记录、试卷和题目选项。
     */
    ExamRecordVO.Detail getDetailRecordById(Integer id);

    /**
     * 查询学生端可参加的试卷，只返回已发布试卷。
     */
    List<Paper> listPublishedPapers(String name);

    /**
     * 提交试卷并完成判卷。
     *
     * 选择题、判断题：后端自动判分。
     * 简答题：调用 AI 判分并生成批阅意见。
     */
    ExamRecordVO.Detail submitPaper(Integer examRecordId, ExamRecordDTO.SubmitPaper submitPaper);

    /**
     * 后台查询考试记录列表。
     */
    List<ExamRecordVO.RecordItem> listExamRecords(ExamRecordDTO.Query query);

    /**
     * 排行榜。
     * paperId 为空时按所有已批阅考试记录排序；不为空时只查某张试卷。
     */
    List<ExamRecordVO.RankingItem> listRanking(Integer paperId);
}
