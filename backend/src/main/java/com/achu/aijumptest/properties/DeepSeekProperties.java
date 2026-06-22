package com.achu.aijumptest.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.sound.sampled.Port;

/**
 * projectName: com.achu.aijumptest.properties.DeepSeekProperties
 *
 * @author: achu_code
 * description: 调用deepseek的五个参数
 */

@Component
@ConfigurationProperties(prefix = "deepseek.api")
@Data
public class DeepSeekProperties {

    private String apikey;
    private String baseUri;
    private String model;
    private Integer maxTokens;
    private Double temperature;
}
