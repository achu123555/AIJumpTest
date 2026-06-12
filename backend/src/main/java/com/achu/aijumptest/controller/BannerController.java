package com.achu.aijumptest.controller;

import com.achu.aijumptest.common.Result;
import com.achu.aijumptest.entity.Banner;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

/**
 * projectName: com.achu.aijumptest.controller.BannerController
 *
 * @author: achu_code
 * description: 轮播图控制层
 */
@RestController
@RequestMapping("/api/banners")
@Tag(name = "轮播图管理",description = "关于轮播图的相关操作,包括图片上传、轮播图增删改查、状态管理等功能")
@Slf4j
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Operation(summary = "获取所有轮播图接口", description = "获取所有轮播图,包括启用和禁用的,供后台管理") //API描述
    @GetMapping("/list")
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

    @Operation(summary = "获取启用的轮播图接口",description = "获取状态为激活的轮播图,供前端首页展示使用")
    @GetMapping("/active")
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

    @Operation(summary = "切换轮播图状态接口",description = "供后台使用的切换轮播图的状态的接口")
    @PutMapping("/toggle/{id}")
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
        if(!update){
            throw new BusinessException("更新轮播图失败！");
        }
        return Result.success();
    }

    @Operation(summary = "删除轮播图接口",description = "此接口为供后台使用的删除轮播图的接口")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteBannerById(
        @Parameter(description = "轮播图ID") @PathVariable("id") Long id
    ){
        //1.执行删除
        log.info("开始删除轮播图,id为:{}",id);
        boolean remove = bannerService.removeById(id);
        //2.打印日志,返回结果
        if(!remove){
            throw new BusinessException("删除轮播图失败！");
        }
        return Result.success();
    }

    @Operation(summary = "根据ID查询轮播图接口",description = "根据ID获取单个轮播图的详情信息")
    @GetMapping("/{id}")
    public Result<Banner> getBannerById(
            @Parameter(description = "轮播图ID") @PathVariable("id") Long id
    ){
        //1.执行查询
        log.info("开始根据ID查询轮播图,ID为:{}",id);
        Banner banner = bannerService.getById(id);
        //2.打印日志,返回结果
        if(banner == null){
            throw new BusinessException("指定ID轮播图不存在。");
        }
        return Result.success(banner);
    }

    @Operation(summary = "上传轮播图接口",description = "将图片上传到阿里云OSS服务器,返回可访问的图片URL")
    @PostMapping("/upload-image")
    public Result<String> uploadBannerImage(
            @Parameter(description = "要上传的图片文件,支持jpg,png,gif等格式,spring默认大小限制10MB")
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        //1.上传到阿里云OSS
        log.info("开始图片文件上传~,图片名字为:{}",file.getOriginalFilename());
        String returnUrl = bannerService.uploadBannerImage(file);

        //2.返回可访问的图片URL
        return Result.success(returnUrl);
    }

    @Operation(summary = "保存轮播图接口",description = "保存填写的轮播图信息和上传的轮播图")
    @PostMapping("/add")
    public Result<Void> addBanner(@RequestBody Banner banner){ //RequestBody的参数文档自动解析实体类的@Schema注解
        //1.执行保存操作
        log.info("开始保存轮播图~,接收的参数为：{}",banner);
        bannerService.saveBanner(banner);
        //2.返回结果
        return Result.success();
    }

    @Operation(summary = "更新轮播图接口",description = "供后台使用的更新轮播图的详细信息接口")
    @PutMapping("/update")
    public Result<Void> updateBanner(@RequestBody Banner banner){
        //1.执行更新操作
        log.info("开始更新轮播图:{}",banner);
        boolean update = bannerService.updateById(banner);
        //2.返回执行结果
        if(!update){
            throw new BusinessException("更新轮播图失败");
        }
        return Result.success();
    }
}
