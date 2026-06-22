package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.excel.QuestionExportExcel;
import com.achu.aijumptest.excel.QuestionImportExcel;
import com.achu.aijumptest.service.AIService;
import com.achu.aijumptest.service.QuestionService;
import com.achu.aijumptest.utils.ExcelUtils;
import com.achu.aijumptest.vo.QuestionPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.controller.QuestionController
 *
 * @author: achu_code
 * description: 题目控制层
 */
@RestController
@RequestMapping("api/questions")
@Tag(name = "题目管理接口")
@Slf4j
public class QuestionController {

    private final QuestionService questionService;
    private final AIService aiService;

    public QuestionController(QuestionService questionService, AIService aiService) {
        this.questionService = questionService;
        this.aiService = aiService;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询题目接口", description = "此接口接收的参数可有可无，结合了条件查询与分页查询功能。")
    public Result<Page<QuestionPageVO>> list(@ParameterObject QuestionDTO.Query queryDTO) {
        // 1. 执行查询
        Page<QuestionPageVO> listByPage = questionService.queryByPageEnhance(queryDTO);

        // 2. 返回结果
        log.info("分页查询题目成功！查询结果为：{}", listByPage.getRecords());
        return Result.success(listByPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看题目详情接口", description = "此接口可以查看题目及详情信息，包括答案和选项。")
    public Result<QuestionPageVO> getById(@PathVariable("id") @Parameter(description = "题目id") Long id) {
        log.info("开始根据 id 查询题目，对应的题目 id 为：{}", id);
        QuestionPageVO questionVo = questionService.getById(id);
        return Result.success(questionVo);
    }

    @PostMapping
    @Operation(summary = "新增题目接口", description = "可以新增题目，题目类型有选择题、判断题、简答题。")
    public Result<Void> save(@RequestBody @ParameterObject QuestionDTO.SaveAndUpdate saveAndUpdateDTO) {
        log.info("开始保存新增题目，题目为：{}", saveAndUpdateDTO);
        questionService.save(saveAndUpdateDTO);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新题目接口", description = "可以编辑题目的所有详情信息。")
    public Result<Void> update(@RequestBody @Parameter(description = "题目更新对象") QuestionDTO.SaveAndUpdate updateDTO) {
        log.info("开始更新题目。要编辑的题目为：{}", updateDTO);
        questionService.update(updateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目接口", description = "根据 id 删除题目及其关联子信息，例如答案和选项。")
    public Result<Void> delete(@PathVariable("id") @Parameter(description = "题目id") Long id) {
        log.info("开始删除题目，题目 id 为：{}", id);
        questionService.delete(id);
        return Result.success();
    }

    @GetMapping("/popular")
    @Operation(summary = "查询热门题目接口", description = "根据传入的 size 参数，查询 size 个热门题目。")
    public Result<List<QuestionPageVO>> getPopularQuestion(@RequestParam("size") @Parameter(description = "查询数量") Integer size) {
        log.info("要查询的热门题目数量为：{}", size);
        List<QuestionPageVO> popularQuestions = questionService.getPopularQuestion(size);
        return Result.success(popularQuestions);
    }


    @PostMapping("/ai/generate")
    @Operation(summary = "AI 批量生成题目接口", description = "根据题目主题、数量、类型、难度和分类调用 AI 生成题目，返回预览列表。")
    public Result<List<QuestionPageVO>> aiGenerateQuestion(@RequestBody QuestionDTO.AiGenerate generateDTO) {
        log.info("开始调用 AI 批量生成题目，参数：{}", generateDTO);
        List<QuestionPageVO> list = aiService.callAiGenerateQuestion(generateDTO);
        return Result.success(list);
    }

    @GetMapping("/template")
    @Operation(summary = "下载题目 Excel 导入模板接口")
    public void downloadTemplate(HttpServletResponse response) {
        // 导入模板只需要表头，所以这里写出空集合。
        // 如果想给用户一行示例数据，可以把 new ArrayList<>() 换成示例数据 List。
        ExcelUtils.writeExcel(
                response,
                new ArrayList<>(),
                "Excel题目导入模板",
                "题目列表",
                QuestionImportExcel.class
        );
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入题目 Excel 接口", description = "上传 Excel 后批量写入 questions、question_choices、question_answer 三张表。")
    public Result<Void> importQuestionExcel(@RequestParam("file") MultipartFile file) {
        log.info("开始批量导入题目 Excel，文件名：{}", file == null ? null : file.getOriginalFilename());
        questionService.importQuestionExcel(file);
        return Result.success();
    }

    @GetMapping("/export")
    @Operation(summary = "批量导出题目 Excel 接口", description = "按照查询条件导出题目列表，包含题目、选项、答案、关键词、解析等信息。")
    public void exportQuestionExcel(@ParameterObject QuestionDTO.Query queryDTO, HttpServletResponse response) {
        log.info("开始批量导出题目 Excel，查询条件：{}", queryDTO);

        // 1. 根据查询条件组装导出数据
        List<QuestionExportExcel> dataList = questionService.listQuestionExport(queryDTO);

        // 2. 写出 Excel 到浏览器
        ExcelUtils.writeExcel(
                response,
                dataList,
                "题目列表",
                "题目列表",
                QuestionExportExcel.class
        );
    }
}
