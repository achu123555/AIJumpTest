package com.achu.aijumptest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * projectName: com.achu.aijumptest.config.AliyunOssProperties
 *
 * @author: achu_code
 * description: 阿里云OSS文件上传的参数类
 */

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {
    private String endpoint;
    private String bucketName;

}
