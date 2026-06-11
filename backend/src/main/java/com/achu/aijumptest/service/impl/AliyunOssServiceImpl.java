package com.achu.aijumptest.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.achu.aijumptest.config.AliyunOssProperties;
import com.achu.aijumptest.service.AliyunOssService;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.*;
import com.aliyuncs.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * projectName: com.achu.aijumptest.service.impl.AliyunOssServiceImpl
 *
 * @author: achu_code
 * description: 阿里云文件上传业务类
 */

@Service
@Slf4j
public class AliyunOssServiceImpl implements AliyunOssService {


    private final String bucketName;
    private final String endpoint;

    private AliyunOssServiceImpl(AliyunOssProperties aliyunOssProperties){
        this.bucketName = aliyunOssProperties.getBucketName();
        this.endpoint = aliyunOssProperties.getEndpoint();
    }
    /**
     * 上传文件功能
     * file: 接口接收的应该是MultipartFile类型的文件
     * folder: 指定存储的分类文件夹
     */
    @Override
    public String fileUpload(MultipartFile file, String folder) throws Exception{
        InputStream in = file.getInputStream();
        String originalName = file.getOriginalFilename();
        assert originalName != null;
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        //获取文件后缀
        String lastFFix = originalName.substring(originalName.lastIndexOf("."));
        //构建唯一文件名
        String fileName = UUID.randomUUID().toString().replaceAll("-","")+lastFFix;
        // 防止 folder 为空
        if (folder == null || folder.trim().isEmpty()) {
            folder = "common";
        }
        // 去掉多余斜杠，避免路径异常
        folder = folder.replaceAll("^/+", "").replaceAll("/+$", "");
        //构建要传输到的文件路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = folder + "/"  + datePath + "/" + fileName; // banner/2026/6/9/asdjoaspdjax.jpg

        // 创建OSSClient实例,当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .build();

        try {
            log.info("[AliYunOSS]开始文件上传,文件名为:{}",originalName);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, in);
            // 创建PutObject请求。
            ossClient.putObject(putObjectRequest);
        } finally {
            ossClient.shutdown();
        }
        // https://ai-jumptest.oss-cn-beijing.aliyuncs.com/banner/2026/6/9/sadasix.jpg
        String returnUrl = "https://"+bucketName+"."+endpoint.split("//")[1]+"/"+objectName;
        log.info("[AliYunOSS]文件上传核心业务完成,返回的URL为:{}",returnUrl);
        return returnUrl;
    }

    /**
     * 分页列举文件
     * @param keyPrefix
     * @param maxSize
     * @return
     */
    public List<String> queryFileByPage(String keyPrefix, int maxSize) throws ClientException {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .build();

        List<String> filesList = new ArrayList<>();
        try {
            String nextMarker = null;
            ObjectListing objectListing;

            do {
                objectListing = ossClient.listObjects(new ListObjectsRequest(bucketName).withPrefix(keyPrefix).withMarker(nextMarker).withMaxKeys(maxSize));

                List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
                //若sums非空 sums转为stream流后遍历key转为集合打印
                if (ObjectUtil.isNotEmpty(sums)) {
                    List<String> keys = sums.stream().map(OSSObjectSummary::getKey).toList();
                    filesList.addAll(keys);
                }

                nextMarker = objectListing.getNextMarker();

            } while (objectListing.isTruncated());
            return filesList;
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 批量删除文件
     * @param objectNames
     * @throws ClientException
     */
    public void deleteBatchFiles(List<String> objectNames) throws ClientException {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .build();

        try {
            // 删除文件。
            // keys每个元素为需要删除的多个文件完整路径。文件完整路径中不能包含Bucket名称。

            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(objectNames).withEncodingType("url"));
            List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

            for(String obj : deletedObjects) {
                String deleteObj =  URLDecoder.decode(obj, StandardCharsets.UTF_8);
                System.out.println(deleteObj);
            }
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
