CREATE TABLE `paper` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                         `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
                         `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'DRAFT',
                         `total_score` decimal(10, 2) NULL DEFAULT 0.00,
                         `question_count` int NULL DEFAULT 0,
                         `duration` int NOT NULL,
                         `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
                         PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '试卷信息表';

CREATE TABLE `paper_question` (
                                  `id` int NOT NULL AUTO_INCREMENT,
                                  `paper_id` int NOT NULL,
                                  `question_id` bigint NOT NULL,
                                  `score` decimal(10, 2) NOT NULL,
                                  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '试卷题目关联表';

INSERT INTO `paper` (`id`, `name`, `description`, `status`, `total_score`, `question_count`, `duration`)
VALUES
    (1, 'Java 后端核心技术综合测验', '涵盖集合、Spring Boot、反射及数据库基础，适合初中级水平考核。', 'PUBLISHED', 40.00, 5, 60),
    (2, 'Java 高级进阶与性能调优专项突破', '聚焦高并发线程池、JVM 垃圾回收与缓存架构高阶挑战。', 'PUBLISHED', 35.00, 3, 45);

INSERT INTO `paper_question` (`paper_id`, `question_id`, `score`)
VALUES
-- ==========================================
-- 试卷 1 (Java 后端核心技术综合测验) 组卷关联
-- ==========================================
(1, 65, 5.00),  -- ArrayList和LinkedList题 (EASY)
(1, 66, 5.00),  -- SpringBoot题 (EASY)
(1, 67, 5.00),  -- Java中的反射题 (MEDIUM)
(1, 72, 5.00),  -- String类被final修饰题 (EASY)
(1, 68, 20.00), -- 数据库事务隔离级别简答题 (MEDIUM)

-- ==========================================
-- 试卷 2 (Java 高级进阶与性能调优专项突破) 组卷关联
-- ==========================================
(2, 70, 5.00),  -- JVM 垃圾回收多选题 (HARD)
(2, 71, 5.00),  -- ThreadPoolExecutor线程池多选题 (MEDIUM)
(2, 69, 25.00); -- 缓存穿透简答题 (HARD)