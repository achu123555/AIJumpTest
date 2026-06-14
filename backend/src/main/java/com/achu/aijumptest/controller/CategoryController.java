package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.entity.Category;
import com.achu.aijumptest.service.CategoryService;
import com.achu.aijumptest.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询所有分类(无子类)
     * @return 分类列表
     */
    @GetMapping
    @Operation(summary = "查询分类列表",description = "获取所有分类列表,没有子分类")
    public Result<List<CategoryVO>> categories(){
        //1.执行查询
        List<CategoryVO> categories = categoryService.getCategories();
        //2.返回结果
        log.info("查询所有题目分类成功！题目分类列表：{}",categories);
        return Result.success(categories);
    }

    /**
     * 查询所有分类树(有子类)
     * @return 分类树状列表
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分类树状列表",description = "查询所有分类列表,包括子分类")
    public Result<List<CategoryVO>> categoryTree(){
        //1.执行查询
        List<CategoryVO> categoryTree = categoryService.getCategoryTree();
        //2.返回结果
        log.info("查询分类树状列表成功！{}",categoryTree);
        return Result.success(categoryTree);
    }

    /**
     * 新增子类分类
     * @param category 子类分类
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "新增子分类接口",description = "此接口只能在父分类下增加子分类")
    public Result<Void> save(
            @Parameter(description = "要新增的子类") @RequestBody Category category
    ){
        //1.执行新增操作
        log.info("开始新增子分类：{}",category);
        categoryService.saveCategory(category);
        //2.返回操作结果
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新子分类接口",description = "编辑保存子分类的名字、排序等级")
    public Result<Void> update(
            @Parameter(description = "子分类") @RequestBody Category category
    ){
        //1.执行更新操作
        log.info("开始执行更新子分类操作：{}",category);
        categoryService.updateCategory(category);
        //2.返回操作结果
        return Result.success();
    }

}

