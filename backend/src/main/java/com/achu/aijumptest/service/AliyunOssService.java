package com.achu.aijumptest.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * projectName: com.achu.aijumptest.service.AliyunOssService
 *
 * @author: achu_code
 * description: 上传文件业务类
 */

public interface AliyunOssService {

    /**
     * 实现文件上传的核心业务方法
     * @param file 上传的文件的封装对象
     * @param folder 用于指定分类的文件夹。例如：轮播图 banner
     * @return 可以直接访问文件的url地址
     * @throws Exception 抛出IO异常
     */
    String fileUpload(MultipartFile file,String folder) throws Exception;
}
