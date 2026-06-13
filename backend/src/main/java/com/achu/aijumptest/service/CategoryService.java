package com.achu.aijumptest.service;


import com.achu.aijumptest.entity.Category;
import com.achu.aijumptest.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.CategoryService
 *
 * @author: achu_code
 * description: 题目分类业务层
 */


public interface CategoryService extends IService<Category> {

    /**
     * 获取所有分类，没有子分类。
     * @return 返回分类列表
     */
    List<CategoryVO> getCategories();

    /**
     * 获取所有分类，包括子分类（分类树）
     * @return 返回树状分类列表
     */
    List<CategoryVO> getCategoryTree();

}
