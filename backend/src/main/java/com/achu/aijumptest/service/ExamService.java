package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.ExamRecordDTO;
import com.achu.aijumptest.entity.ExamRecord;
import com.achu.aijumptest.vo.ExamRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * projectName: com.achu.aijumptest.service.ExamService
 *
 * @author: achu_code
 * description: 考试业务层接口
 */

public interface ExamService extends IService<ExamRecord> {

    /**
     *  开始考试
     *  --新增考试记录
     *  --已存在则校验考试状态,正在进行的返回此记录对象即可。
     * @param startExam 试卷id和考生姓名
     * @return 考试记录对象
     */
    ExamRecord startExam(ExamRecordDTO.startExam startExam);

    /**
     * 获取考试记录详情
     *  --包括基本信息和作答记录
     *  --试卷和题目及选项答案
     * @param id
     * @return
     */
    ExamRecordVO.Detail getDetailRecordById(Integer id);
}
