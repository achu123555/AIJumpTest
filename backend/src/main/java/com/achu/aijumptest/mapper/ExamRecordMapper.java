package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.ExamRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * projectName: com.achu.aijumptest.mapper.ExamRecordMapper
 *
 * @author: achu_code
 * description: 考试记录数据访问层
 */

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
}
