package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.Paper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * projectName: com.achu.aijumptest.mapper.PaperMapper
 *
 * @author: achu_code
 * description: 试卷数据访问层
 */

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {
}
