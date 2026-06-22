package com.achu.aijumptest.config;

import com.achu.aijumptest.properties.DeepSeekProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * projectName: com.achu.aijumptest.config.WebClientConfiguration
 *
 * @author: achu_code
 * description: 构建连接deepseek的webClient对象
 */

@Configuration
@EnableConfigurationProperties
public class WebClientConfiguration {

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    @Bean
    public WebClient webClient(){
        return WebClient.builder()
                .baseUrl(deepSeekProperties.getBaseUri())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization","Bearer "+deepSeekProperties.getApikey())
                .build();
    }
}
