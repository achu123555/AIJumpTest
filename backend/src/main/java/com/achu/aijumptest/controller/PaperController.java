package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
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
    public Result<PaperVO.Detail> getById(@Parameter(description = "试卷id") @PathVariable("id") Long id){

        PaperVO.Detail detailPaper = paperService.getDetailPaper(id);

        return Result.success(detailPaper);
    }
}
