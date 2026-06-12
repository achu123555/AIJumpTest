package com.achu.aijumptest.service.impl;

import com.achu.aijumptest.entity.Banner;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.BannerMapper;
import com.achu.aijumptest.service.AliyunOssService;
import com.achu.aijumptest.service.BannerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * projectName: com.achu.aijumptest.service.impl.BannerServiceImpl
 *
 * @author: achu_code
 * description: 轮播图业务层实现类01
 */

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Autowired
    private AliyunOssService aliyunOssService;
    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public String uploadBannerImage(MultipartFile file) throws Exception {
        //1.非空校验
        //file == null —— 请求里完全没有这个 multipart 字段
        //file.isEmpty() —— 字段存在，但文件内容为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传轮播图图片失败。原因：上传的文件为空。");
        }
        //2.格式校验
        //.getContentType : image / jpg jpeg
        String contentType = file.getContentType();
        assert contentType != null;
        if (!contentType.startsWith("image")) {
            throw new BusinessException("上传轮播图图片失败。原因：图片上传类型错误。");
        }
        //3.调用上传文件业务
        String returnUrl = aliyunOssService.fileUpload(file, "banner");

        //4.返回地址
        return returnUrl;
    }

    @Override
    public void saveBanner(Banner banner) {
        //参数判空：sort为空默认为5
        if(banner.getSortOrder() == null){
            banner.setSortOrder(5);
        }
        //参数判空：isActive为空默认为true
        if(banner.getIsActive() == null){
            banner.setIsActive(true);
        }
        //保存轮播图
        int insert = bannerMapper.insert(banner);
        if(insert != 1){
            throw new BusinessException("保存轮播图失败");
        }
    }
}
