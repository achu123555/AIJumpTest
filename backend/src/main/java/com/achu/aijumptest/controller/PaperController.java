package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.dto.PaperDTO;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.service.PaperService;
import com.achu.aijumptest.vo.PaperVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.controller.PaperController
 *
 * @author: achu_code
 * description: 试卷控制层
 */

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
@Slf4j
public class PaperController {

    private final PaperService paperService;

    @GetMapping("/list")
    @Operation(summary = "查询所有试卷接口")
    public Result<List<Paper>> list(
            @Parameter(description = "试卷名称") @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "试卷状态") @RequestParam(name = "status", required = false) String status
    ){
        log.info("开始查询试卷列表,查询参数: (name:{}  status:{})",name,status);
        List<Paper> papers = paperService.list(
                new LambdaQueryWrapper<Paper>()
                        .like(ObjectUtils.isNotEmpty(name), Paper::getName, name)
                        .eq(ObjectUtils.isNotEmpty(status), Paper::getStatus, status)
        );
        return Result.success(papers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "试卷详情接口",description = "查询试卷的详情信息包括题目及答案选项")
    public Result<PaperVO.Detail> getById(@Parameter(description = "试卷id") @PathVariable("id") Integer id){
        log.info("要查询试卷详情的试卷id为:{}",id);
        PaperVO.Detail detailPaper = paperService.getDetailPaper(id);

        return Result.success(detailPaper);
    }

    @PostMapping
    @Operation(summary = "手动组卷接口")
    public Result<Paper> createPaper(@RequestBody PaperDTO.Create create){
        log.info("开始创建试卷,内容为：{}", create);
        Paper paper = paperService.createPaper(create);
        return Result.success(paper);
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑试卷接口")
    public Result<Paper> update(
            @Parameter(description = "试卷id") @PathVariable("id") Integer id,
            @RequestBody PaperDTO.Create update
    ){
        log.info("开始更新试卷,试卷id为：{},更改信息为：{}",id,update);
        Paper paper = paperService.update(id,update);
        return Result.success(paper);
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "切换试卷状态接口",description = "传入id和status参数来切换试卷的状态，没有题目不可发布！")
    public Result<Void> switchStatus(
            @Parameter(description = "试卷id") @PathVariable("id") Integer id,
            @RequestParam("status") String status
    ){
        log.info("开始切换试卷状态,要切换的试卷id和状态为:{}-{}",id,status);
        paperService.switchStatus(id,status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除试卷接口")
    public Result<Void> delete(
            @Parameter(description = "试卷id") @PathVariable("id") Integer id
    ){
        log.info("开始删除试卷,要删除的试卷id为：{}",id);
        paperService.remove(id);
        return Result.success();
    }
}
