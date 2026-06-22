package com.achu.aijumptest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class AiJumpTestApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void AICalledTest(){


        // 1.初始化WebClient(设置baseurl、内容类型、认证参数)
        String apiKey = "apikey";
        String baseUrl = "https://api.deepseek.com";

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl + "/chat/completions")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();


        // 2.封装JSON

        Map<String, String> systemMap = new HashMap<>();
        systemMap.put("role","system");
        systemMap.put("content","你是deepseek,为用户提供咨询问答功能");
        Map<String, String> userMap = new HashMap<>();
        userMap.put("role","user");
        userMap.put("content","你好,我是李雷,1+1=多少？");

        List<Map<String,String>> msgList = new ArrayList<>();
        msgList.add(systemMap);
        msgList.add(userMap);

        HashMap<String,Object> requestBody = new HashMap<>();
        requestBody.put("model","deepseek-v4-flash");
        requestBody.put("messages",msgList);
        requestBody.put("temperature",0.6);

        // 3.发起网络请求
        Mono<String> mono = webClient.post()
                .bodyValue(requestBody)
                .retrieve() //准备就绪
                .bodyToMono(String.class) //返回值类型
                .timeout(Duration.ofSeconds(100));

        // 4.WebClient同步与异步请求
        //同步
        String result = mono.block();
        System.out.println(result);
        //异步 → 调用后,程序继续向后执行,请求结束后调用回调函数
        mono.subscribe(System.out::println);

    }

}
