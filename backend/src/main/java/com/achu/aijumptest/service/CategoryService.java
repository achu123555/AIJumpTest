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

    /**
     * 保存子分类
     * @param category 子分类实体类
     */
    void saveCategory(Category category);

    /**
     * 更新子分类
     * @param category 子分类实体类
     */
    void updateCategory(Category category);

    /**
     * 删除子分类
     * @param id 分类id
     */
    void removeCategoryById(Long id);
}
