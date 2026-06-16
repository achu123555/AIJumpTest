package com.achu.aijumptest.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * projectName: com.achu.aijumptest.config.MybatisPlusConfiguration
 *
 * @author: achu_code
 * description: mp配置类
 */

@Configuration
public class MybatisPlusConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor PagePlugin = new PaginationInnerInterceptor();
        PagePlugin.setDbType(DbType.MYSQL);
        PagePlugin.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(PagePlugin);
        return interceptor;
    }
}
