package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.service.QuestionService;
import com.achu.aijumptest.vo.QuestionPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * projectName: com.achu.aijumptest.controller.QuestionController
 *
 * @author: achu_code
 * description: 题目控制层
 */

@RestController
@RequestMapping("api/questions")
@Tag(name = "题目管理",description = "此接口包含题目的增删改查等功能")
@Slf4j
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService){
        this.questionService = questionService;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询题目接口",description = "此接口接收的参数可有可无,结合了条件查询与分页查询功能。")
    public Result<Page<QuestionPageVO>> list(@ParameterObject QuestionDTO.Query queryDTO){

        //1.执行查询
        Page<QuestionPageVO> listByPage = questionService.queryByPageEnhance(queryDTO);
        //2.返回结果
        log.info("分页查询题目成功！查询结果为:{}",listByPage.getRecords());
        return Result.success(listByPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看题目详情接口",description = "此接口可以查看题目及详情信息,包括答案和选项。")
    public Result<QuestionPageVO> getById(
            @PathVariable("id") @Parameter(description = "题目id") Long id){
        log.info("开始根据id查询题目,对应的题目id为：{}",id);
        QuestionPageVO questionVo = questionService.getById(id);

        return Result.success(questionVo);
    }

    @PostMapping
    @Operation(summary = "新增题目接口",description = "可以新增题目,题目类型有选择题、多选题、简答题")
    public Result<Void> save(
            @RequestBody @ParameterObject QuestionDTO.Save saveDTO
    ){
        //1.执行保存
        log.info("开始保存新增题目，题目为：{}",saveDTO);
        questionService.save(saveDTO);
        //2.返回结果
        return Result.success();
    }
}
