package com.achu.aijumptest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * projectName: com.achu.aijumptest.config.Knife4jConfiguration
 *
 * @author: achu_code
 * description: knife4j配置类
 */

@Configuration
public class Knife4jConfiguration {

    /**
     * 配置OpenAPI文档信息
     * 设置API文档的标题、描述、版本等基本信息
     * Knife4j会自动读取这些信息并生成美观的文档页面
     * * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🎓 智能考试系统API文档") // API文档标题，添加emoji图标
                        .description("📘 智能考试系统后端接口文档，提供完整的RESTful API服务\n\n" +
                                "✨ 主要功能模块：\n" +
                                "· 💬 题目管理：支持选择题、判断题、简答题的增删改查\n" +
                                "· 📝 试卷管理：手动组卷和AI智能组卷\n" +
                                "· 🎨 轮播图管理：首页轮播图的图片上传和管理\n" +
                                "· 📊 考试记录：考试结果统计和分析\n" +
                                "· 🔔 公告管理：系统公告的发布和管理") // API文档描述，使用markdown格式
                        .version("v1.0.0"));
    }

    /**
     * 设置接口分组：轮播图业务分组
     */
    @Bean
    public GroupedOpenApi bannerApi() {
        return GroupedOpenApi.builder()
                //接口名
                .group("轮播图管理接口")
                //通过路由来匹配
                .pathsToMatch("/api/banners/**")
                .build();
    }

    /**
     * 设置接口分组：题目分类业务分组
     */
    @Bean
    public GroupedOpenApi categoryApi() {
        return GroupedOpenApi.builder()
                //接口名
                .group("题目分类管理接口")
                //通过路由来匹配
                .pathsToMatch("/api/categories/**")
                .build();
    }

    /**
     * 设置接口分组：题目业务分组
     */
    @Bean
    public GroupedOpenApi questionApi() {
        return GroupedOpenApi.builder()
                //接口名
                .group("题目管理接口")
                //通过路由来匹配
                .pathsToMatch("/api/questions/**")
                .build();
    }

    /**
     * 设置接口分组：试卷业务分组
     */
    @Bean
    public GroupedOpenApi paperApi() {
        return GroupedOpenApi.builder()
                //接口名
                .group("试卷管理接口")
                //通过路由来匹配
                .pathsToMatch("/api/papers/**")
                .build();
    }

    /**
     * 设置接口分组：考试记录业务分组
     */
    @Bean
    public GroupedOpenApi examRecordApi() {
        return GroupedOpenApi.builder()
                //接口名
                .group("考试记录管理接口")
                //通过路由来匹配
                .pathsToMatch("/api/exam/**")
                .build();
    }


}
