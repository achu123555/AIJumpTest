package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.entity.Banner;
import com.achu.aijumptest.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.controller.BannerController
 *
 * @author: achu_code
 * description: 轮播图控制层
 */
@RestController
@RequestMapping("api/banners")
@Tag(name = "轮播图管理",description = "关于轮播图的相关操作,包括图片上传、轮播图增删改查、状态管理等功能")
@Slf4j
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("list")
    @Operation(summary = "获取所有轮播图", description = "获取所有轮播图,包括启用和禁用的,供后台管理") //API描述
    public Result<List<Banner>> list(){
        //1.包装查询条件
        LambdaQueryWrapper<Banner> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(Banner::getSortOrder);//根据排序字段升序排序
        //2.查询所有符合条件的集合
        List<Banner> bannerList = bannerService.list(lambdaQueryWrapper);
        //3.返回结果
        log.info("查询所有轮播图业务执行成功,结果为:{}",bannerList);
        return Result.success(bannerList);
    }


}
