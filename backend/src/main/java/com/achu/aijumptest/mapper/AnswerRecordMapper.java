package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.AnswerRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * projectName: com.achu.aijumptest.mapper.AnswerRecordMapper
 *
 * @author: achu_code
 * description: 作答记录数据访问层
 */

@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {
}
