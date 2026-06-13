package com.achu.aijumptest.mapper;

import com.achu.aijumptest.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * projectName: com.achu.aijumptest.mapper.CategoryMapper
 *
 * @author: achu_code
 * description: 题目分类数据访问层
 */

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
