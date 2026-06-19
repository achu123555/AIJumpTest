package com.achu.aijumptest.common;

/**
 * projectName: com.achu.aijumptest.common.CacheConstants
 *
 * @author: achu_code
 * description: 缓存常量类
 */

public class CacheConstants {

    /**
     * 热门题目 Redis Key 前缀
     * 规范：项目名:模块名:业务名
     * 最终在 Redis 里呈现出来的完整 Key 叫 "aijumptest:question:popular"
     */
    public static final String POPULAR_QUESTIONS_KEY = "aijumptest:question:popular";

}
