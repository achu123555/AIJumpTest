package com.achu.aijumptest.metaobject;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * projectName: com.achu.aijumptest.metaobject.MyMetaObjectHandler
 *
 * @author: achu_code
 * description: 自动填充公共参数
 */

@Component //加入到IOC容器中,供mybatis-plus使用
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("【MyBatis-Plus】触发插入动作开始自动填充通用参数...");
        // 自动将当前系统时间填充到 Java 对象的 createTime 和 updateTime 属性
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 自动将 0 喂给 isDeleted 属性
        this.strictInsertFill(metaObject, "isDeleted", Boolean.class, false);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("【MyBatis-Plus】触发更新动作开始填充通用参数...");
        // 更新时，自动刷新 updateTime 属性为当前时间
        this.strictInsertFill(metaObject,"updateTime", LocalDateTime.class,LocalDateTime.now());
    }
}
