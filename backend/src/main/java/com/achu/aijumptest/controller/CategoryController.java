package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.entity.Category;
import com.achu.aijumptest.service.CategoryService;
import com.achu.aijumptest.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.controller.CategoryController
 *
 * @author: achu_code
 * description: 题目分类模块控制层
 */

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分类管理", description = "关于题目分类的增删改查等动作。")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "查询分类列表",description = "获取所有分类列表,没有子分类")
    public Result<List<CategoryVO>> categories(){
        //1.执行查询
        List<CategoryVO> categories = categoryService.getCategories();
        //2.返回结果
        log.info("查询所有题目分类成功！题目分类列表：{}",categories);
        return Result.success(categories);
    }

    @GetMapping("/tree")
    @Operation(summary = "查询分类树状列表",description = "查询所有分类列表,包括子分类")
    public Result<List<CategoryVO>> categoryTree(){
        //1.执行查询
        List<CategoryVO> categoryTree = categoryService.getCategoryTree();
        //2.返回结果
        log.info("查询分类树状列表成功！{}",categoryTree);
        return Result.success(categoryTree);
    }

}

