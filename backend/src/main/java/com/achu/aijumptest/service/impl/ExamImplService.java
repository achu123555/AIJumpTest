package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.dto.ExamRecordDTO;
import com.achu.aijumptest.entity.AnswerRecord;
import com.achu.aijumptest.entity.ExamRecord;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.AnswerRecordMapper;
import com.achu.aijumptest.mapper.ExamRecordMapper;
import com.achu.aijumptest.mapper.PaperMapper;
import com.achu.aijumptest.service.ExamService;
import com.achu.aijumptest.service.PaperService;
import com.achu.aijumptest.vo.ExamRecordVO;
import com.achu.aijumptest.vo.PaperVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.impl.ExamImplService
 *
 * @author: achu_code
 * description: 考试业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExamImplService extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamService {

    private final PaperMapper paperMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final PaperService paperService;

    @Override
    public ExamRecord startExam(ExamRecordDTO.startExam startExam) {

        // 1. 校验试卷合法性（防止越权开考草稿卷、不存在的卷）
        Paper paper = paperMapper.selectById(startExam.getPaperId());
        if (paper == null || !"PUBLISHED".equals(paper.getStatus())) {
            throw new BusinessException("该试卷不存在或未正式发布，无法开始考试！");
        }

        // 2. 校验(X试卷已存在且状态处于进行中,则返回该考试记录)
        ExamRecord examRecord = baseMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getPaperId, startExam.getPaperId())
                        .eq(ExamRecord::getStudentName, startExam.getStudentName())
                        .eq(ExamRecord::getStatus, "进行中")
        );
        if(examRecord != null){
            log.debug("考生{}在id为{}的试卷有正在进行的考试记录,直接返回此考试记录!",startExam.getStudentName(),startExam.getPaperId());
            return examRecord;
        }
        // 3.防御高并发双击（利用最轻量的标准 JVM 锁，确保同一个学生并发连击时不会写重）
        String lockKey = (startExam.getStudentName() + "_" + startExam.getPaperId()).intern();
        synchronized (lockKey) {
            // 二次检查（Double-Check），防止排队进来的请求重复插入
            ExamRecord doubleCheck = baseMapper.selectOne(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getPaperId, startExam.getPaperId())
                            .eq(ExamRecord::getStudentName, startExam.getStudentName())
                            .eq(ExamRecord::getStatus, "进行中")
            );
            if (doubleCheck != null) {
                return doubleCheck;
            }
        }
        // 4.如果该考试记录不存在,则新增考试记录
        examRecord = new ExamRecord();
        examRecord.setPaperId(startExam.getPaperId());
        examRecord.setStudentName(startExam.getStudentName());
        examRecord.setStatus("进行中");
        examRecord.setWindowSwitches(0);
        examRecord.setStartTime(LocalDateTime.now());
        // 5.保存考试记录,返回
        baseMapper.insert(examRecord);
        return examRecord;

    }

    @Override
    public ExamRecordVO.Detail getDetailRecordById(Integer id) {
        //1.查询校验考试记录是否存在
        ExamRecord examRecord = baseMapper.selectById(id);
        if(examRecord == null){
            throw new BusinessException("该考试记录不存在！");
        }
        //2.查询校验作答记录是否存在
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getExamRecordId, examRecord.getId())
        );
        if(ObjectUtils.isEmpty(answerRecords)){
            //作答记录不存在,返回空列表
            answerRecords = new ArrayList<>();
        }
        //3.查询试卷+题目(内部已有校验)
        PaperVO.Detail detailPaper = paperService.getDetailPaper(examRecord.getPaperId());
        //4.填充数据,返回
        ExamRecordVO.Detail detailRecord = BeanUtil.copyProperties(examRecord, ExamRecordVO.Detail.class);
        detailRecord.setAnswerRecords(answerRecords);
        detailRecord.setDetailPaper(detailPaper);
        return detailRecord;
    }
}
