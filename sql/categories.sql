CREATE TABLE `categories` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                              `parent_id` bigint NULL DEFAULT 0,
                              `sort` int NULL DEFAULT 0,
                              `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '题目分类表';

INSERT INTO `categories` (`id`, `name`, `parent_id`, `sort`) VALUES
--  一级分类：固定三大题型
(1, '选择题', 0, 1),
(2, '判断题', 0, 2),
(3, '简答题', 0, 3),

--  二级分类：属于“选择题”(parent_id = 1)
(4, 'Java基础', 1, 1),
(5, 'Spring框架', 1, 2),

--  二级分类：属于“判断题”(parent_id = 2)
(6, 'Java核心技术', 2, 1),

--  二级分类：属于“简答题”(parent_id = 3)
(7, 'MySQL数据库', 3, 1),
(8, 'Redis分布式缓存', 3, 2);
