package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.entity.Banner;
import com.achu.aijumptest.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
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
    public Result<List<Banner>> getAllBanners(){
        //1.包装查询条件(sort字段升序)
        LambdaQueryWrapper<Banner> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(Banner::getSortOrder);//根据排序字段升序排序
        //2.返回符合查询条件的集合
        List<Banner> bannerList = bannerService.list(lambdaQueryWrapper);
        //3.包装结果返回
        log.info("查询所有轮播图业务执行成功,结果为:{}",bannerList);
        return Result.success(bannerList);
    }

    @GetMapping("active")
    @Operation(summary = "获取启用的轮播图",description = "获取状态为激活的轮播图,供前端首页展示使用")
    public Result<List<Banner>> getActiveBanners(){
        //1.包装查询条件(active==1,sort字段升序)
        LambdaQueryWrapper<Banner> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Banner::getIsActive,1); //active = 1
        lambdaQueryWrapper.orderByAsc(Banner::getSortOrder); //sort字段升序排序
        //2.返回符合查询条件的集合
        List<Banner> bannerList = bannerService.list(lambdaQueryWrapper);
        //3.包装结果返回
        log.info("查询前端所需要的激活状态的轮播图业务执行成功！结果为{}",bannerList);
        return Result.success(bannerList);
    }

    @PutMapping("toggle/{id}")
    @Operation(summary = "切换轮播图状态接口",description = "供后台使用的切换轮播图的状态的接口")
    public Result<Void> toggleBannerStatus(
            @Parameter(description = "轮播图ID") @PathVariable("id") Long id,
            @Parameter(description = "轮播图状态") @RequestParam("isActive") Boolean isActive //要求前端必须传递
            ){
        //1.包装更新条件(判断id,放置isActive)
        LambdaUpdateWrapper<Banner> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Banner::getId,id)
                .set(Banner::getIsActive,isActive);
        //2.执行更新
        boolean update = bannerService.update(updateWrapper);
        //3.返回结果
        if(update){
            log.info("切换轮播图状态成功,此时状态为:{}",isActive);
            return Result.success();
        }
        log.info("切换轮播图状态失败,计划目标状态为:{}",isActive);
        return Result.error("切换轮播图状态失败");
    }

    @DeleteMapping("delete/{id}")
    @Operation(summary = "删除轮播图接口",description = "此接口为供后台使用的删除轮播图的接口")
    public Result<Void> deleteBanner(
        @Parameter(description = "轮播图ID") @PathVariable("id") Long id
    ){
        //1.执行删除
        boolean remove = bannerService.removeById(id);
        //2.打印日志,返回结果
        if(remove){
            log.info("删除成功！本次删除的轮播图id为{}",id);
            return Result.success();
        }

        log.info("删除失败！本次删除失败的轮播图id为{}",id);
        return Result.error("删除失败");
    }
}
