
CREATE TABLE `questions` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
                             `category_id` bigint NOT NULL COMMENT '题目分类ID(关联categories表的id)',
                             `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干/题目内容',
                             `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '选项内容(JSON格式，如选择题：["A.xx","B.xx"])',
                             `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参考答案(选择题存A/B/C,判断题存0/1,简答题存文本)',
                             `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI或专家给出的题目解析',
                             `difficulty` tinyint NULL DEFAULT 1 COMMENT '难度等级：1-简单, 2-中等, 3-困难',
                             `default_score` int NULL DEFAULT 5 COMMENT '默认分值',
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
                             PRIMARY KEY (`id`) USING BTREE,
                             INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB COMMENT = '题目表' ROW_FORMAT = Dynamic;


INSERT INTO `questions`
(`category_id`, `content`, `options`, `answer`, `analysis`, `difficulty`, `default_score`)
VALUES
--  题1：Java 基础选择题 (关联 category_id = 4)
(4,
 '在 Java 中，关于 String 对象的比较，以下说法正确的是？',
 '["A. == 永远比较的是两个字符串的内容是否相等", "B. equals() 方法在 String 类中未被重写", "C. == 比较的是内存地址，equals() 被重写后比较的是内容", "D. 两者没有任何区别"]',
 'C',
 '【AI 智能解析】Java 中 == 用于比较基本数据类型的值或引用类型的内存地址。String 类重写了 Object 的 equals() 方法，改变了其原有的地址比较行为，将其实现为逐个字符比较内容是否相等。因此正确答案为 C。',
 1, 5),

--  题2：Spring 框架选择题 (关联 category_id = 5)
(5,
 'Spring 框架的核心思想 AOP 是指什么？',
 '["A. 控制反转", "B. 面向切面编程", "C. 依赖注入", "&D. 面向对象编程"]',
 'B',
 '【AI 智能解析】AOP 全称为 Aspect Oriented Programming，即面向切面编程。它利用横切技术，将那些与业务无关、却为业务模块所共同调用的逻辑（如日志管理、权限控制、事务处理）封装起来，达到解耦的目的。',
 2, 5),

--  题3：Java 判断题 (关联 category_id = 6，无需选项)
(6,
 'Java 中的 HashMap 是线程安全的，可以在多线程环境下直接并发使用。',
 NULL,
 '0',
 '【AI 智能解析】错误。HashMap 是非线程安全的。在多线程高并发环境下，其 put 操作可能会导致数据覆盖或死循环（JDK7 的死链问题，JDK8 后为树化红黑树导致的哈希碰撞问题）。若需线程安全，应使用 ConcurrentHashMap。',
 1, 2),

--  题4：MySQL 简答题 (关联 category_id = 7，答案和解析内容较长)
(7,
 '请简述 MySQL 中索引失效的常见场景有哪些？',
 NULL,
 '1. 违背最左前缀法则；\n2. 在索引列上进行运算、函数或类型转换操作；\n3. 使用了 select * 导致无法索引覆盖；\n4. 模糊查询以 % 开头；\n5. or 连接的条件中存在非索引列。',
 '【AI 智能解析】索引失效会导致全表扫描。编写 SQL 时应极力避免对索引列进行 `YEAR(create_time)` 等函数操作，同时在模糊查询时严禁使用 `%keyword` 的前缀模糊检索。',
 3, 10);