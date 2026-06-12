package com.achu.aijumptest.service;

import com.achu.aijumptest.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * projectName: com.achu.aijumptest.service.BannerService
 *
 * @author: achu_code
 * description: 轮播图业务层
 */

public interface BannerService extends IService<Banner> {

    /**
     * 上传轮播图业务！ 做校验 + 调用上传文件业务方法
     * @param file 要上传的文件
     * @return 图片回显的地址
     */
    String uploadBannerImage(MultipartFile file) throws Exception;

    void saveBanner(Banner banner);
}
