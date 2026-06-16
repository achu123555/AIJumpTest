CREATE TABLE `questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `multi` tinyint(1) NULL DEFAULT 0,
  `category_id` bigint NOT NULL,
  `difficulty` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `score` int NULL DEFAULT 5,
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE `question_choices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_correct` tinyint(1) NULL DEFAULT 0,
  `sort` int NULL DEFAULT 0,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE `question_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `keywords` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ====================================================================
-- 题 1：Java基础分类 (ID: 4) - 单选题 (CHOICE, multi=0)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('Java中关于ArrayList和LinkedList的性能与底层结构，以下说法正确的是？', 'CHOICE', 0, 4, 'EASY', 5, '【AI 智能解析】ArrayList 底层是动态数组，连续内存寻址极快，适合随机查找（O(1)）；LinkedList 底层是双向链表，头尾增删极快（O(1)），但查找需要遍历。因此正确答案选择 C。');

--  抓取刚自增生成的题目ID存入局部变量
SET @q_id1 = LAST_INSERT_ID();

INSERT INTO `question_choices` (`question_id`, `content`, `is_correct`, `sort`) VALUES
(@q_id1, 'ArrayList底层是链表，增删速度永远快于LinkedList', 0, 1),
(@q_id1, 'LinkedList底层是动态数组，内存空间必须连续', 0, 2),
(@q_id1, 'ArrayList适合随机查找，LinkedList适合头尾快速插入与删除', 1, 3),
(@q_id1, '在任何业务场景下，两者的执行效率和内存开销完全没有区别', 0, 4);

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id1, 'C', NULL);


-- ====================================================================
-- 题 2：Spring框架分类 (ID: 5) - 单选题 (CHOICE, multi=0)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('在 Spring Boot 开发中，日常高频使用的 @RestController 注解，其底层等同于哪两个注解的组合功能？', 'CHOICE', 0, 5, 'EASY', 5, '【AI 智能解析】@RestController 是一个组合注解，它将标准控制器 @Controller 和响应体响应 @ResponseBody 融合在一起，使得该类中所有方法的返回值都会直接转化为 JSON 字符串输出给前端。');

SET @q_id2 = LAST_INSERT_ID();

INSERT INTO `question_choices` (`question_id`, `content`, `is_correct`, `sort`) VALUES
(@q_id2, '@Controller 和 @ResponseBody', 1, 1),
(@q_id2, '@Component 和 @RequestMapping', 0, 2),
(@q_id2, '@Service 和 @ResponseBody', 0, 3),
(@q_id2, '@Repository 和 @Controller', 0, 4);

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id2, 'A', NULL);


-- ====================================================================
-- 题 3：Java核心技术 (ID: 6) - 判断题 (JUDGE, multi=0)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('Java中的反射（Reflection）虽然功能强大，但在高性能、高并发场景下频繁使用会导致系统性能明显下降。', 'JUDGE', 0, 6, 'MEDIUM', 2, '【AI 智能解析】正确。反射涉及动态类型解析和安全检查，绕过了 JVM 的很多静态优化，且无法充分享受 JIT（即时编译器）的内联优化，耗时比直接调用高数倍，高并发下应尽量通过缓存其元数据来优化。');

SET @q_id3 = LAST_INSERT_ID();

-- 注：判断题和简答题不需要往 choices 表插数据
INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id3, 'TRUE', '反射,性能下降,动态解析,JIT优化');


-- ====================================================================
-- 题 4：MySQL数据库 (ID: 7) - 简答题 (TEXT, multi=0)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('请简述什么是数据库事务隔离级别中的“脏读（Dirty Read）”？并给出主流的解决方案。', 'TEXT', 0, 7, 'MEDIUM', 10, '【AI 智能解析】脏读指事务A读取了事务B尚未提交的修改数据，若事务B随后回滚，则事务A读到的数据为脏数据。标准解法是将隔离级别提升到“读已提交（Read Committed）”或以上，MySQL 默认利用 MVCC 机制在“可重复读”级别下彻底解决了此问题。');

SET @q_id4 = LAST_INSERT_ID();

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id4, '脏读是指一个事务读取了另一个事务尚未提交的数据。解决方法是将数据库的事务隔离级别提高到“读已提交（Read Committed）”或使用锁与MVCC机制。', '尚未提交,读已提交,隔离级别,MVCC');


-- ====================================================================
-- 题 5：Redis分布式缓存 (ID: 8) - 简答题 (TEXT, multi=0)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('请解释什么是缓存穿透（Cache Penetration）？并写出企业最常用的两种防护手段。', 'TEXT', 0, 8, 'HARD', 10, '【AI 智能解析】缓存穿透指高并发查询一个在缓存和数据库中都绝对不存在的数据，导致大量请求直接打穿缓存轰炸数据库。两大利器：1. 缓存空对象/错误标识；2. 在缓存前置布隆过滤器（Bloom Filter）进行高精准拦截。');

SET @q_id5 = LAST_INSERT_ID();

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id5, '缓存穿透指查询根本不存在的数据，缓存和库都不命中。常见解决方案：1. 缓存空值（配置短过期时间）；2. 使用布隆过滤器进行前置拦截。', '不存在,缓存空值,布隆过滤器,打穿');


-- ====================================================================
-- 题 6：Java进阶分类 (ID: 9) - 多选题 (CHOICE, multi=1)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('关于 JVM 垃圾回收（GC）机制中，G1（Garbage-First）垃圾收集器的核心特点，以下说法正确的有？', 'CHOICE', 1, 9, 'HARD', 5, '【AI 智能解析】G1 颠覆了传统物理隔离分区，将堆分成独立的 Region（A对）；它依然保留逻辑上的年轻代老年代概念（B错）；它使用标记-复制算法清理，无内存碎片（C对）；最大亮点是允许用户指定可预测的停顿时间目标（D对）。');

SET @q_id6 = LAST_INSERT_ID();

INSERT INTO `question_choices` (`question_id`, `content`, `is_correct`, `sort`) VALUES
(@q_id6, '它将整个堆内存划分为多个大小相等的独立区域（Region）', 1, 1),
(@q_id6, '它彻底取消了新生代和老年代的概念，属于完全无分代的收集器', 0, 2),
(@q_id6, '它在局部清理 Region 时采用的是标记-复制算法，能有效避免内存碎片', 1, 3),
(@q_id6, '它允许用户指定在一段指定时间内，因垃圾回收造成的最大停顿时间（可预测停顿）', 1, 4);

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id6, 'A,C,D', NULL);


-- ====================================================================
-- 题 7：Java多线程分类 (ID: 10) - 多选题 (CHOICE, multi=1)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('在使用线程池（ThreadPoolExecutor）时，若核心线程已满且阻塞队列也堆满，后续新任务将触发拒绝策略。以下属于 JDK 内置标准拒绝策略的有？', 'CHOICE', 1, 10, 'MEDIUM', 5, '【AI 智能解析】JDK 提供了 4 种拒绝策略：直接抛异常（Abort）、用调用者线程执行（CallerRuns）、悄悄丢弃最新任务（Discard）、丢弃队列最老任务（DiscardOldest）。RetryPolicy 不属于系统内置标准策略，故 D 错。');

SET @q_id7 = LAST_INSERT_ID();

INSERT INTO `question_choices` (`question_id`, `content`, `is_correct`, `sort`) VALUES
(@q_id7, 'AbortPolicy（直接抛出 RejectedExecutionException 异常终止）', 1, 1),
(@q_id7, 'CallerRunsPolicy（将任务退回给调用者所在的线程来直接同步执行）', 1, 2),
(@q_id7, 'DiscardOldestPolicy（默默丢弃阻塞队列中处于最前端、等待最久的任务）', 1, 3),
(@q_id7, 'RetryPolicy（自动开启无限后台重试重投任务，直到执行成功为止）', 0, 4);

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id7, 'A,B,C', NULL);


-- ====================================================================
-- 题 8：Java核心技术 (ID: 6) - 判断题 (JUDGE, multi=0)
-- ====================================================================
INSERT INTO `questions` (`title`, `type`, `multi`, `category_id`, `difficulty`, `score`, `analysis`)
VALUES
('Java中的 String 类被 final 关键字修饰，这意味着每一次对 String 字符串进行拼接或修改（如用 + 号拼接），在底层都会创建全新的 String 对象（暂不考虑字面量编译期优化）。', 'JUDGE', 0, 6, 'EASY', 2, '【AI 智能解析】正确。因为 String 是不可变（Immutable）的，所有的修改、替换、拼接操作都会在堆中（或字符串常量池外）产生全新的字符串对象，这也是高频拼接推荐使用 StringBuilder 的底层根源。');

SET @q_id8 = LAST_INSERT_ID();

INSERT INTO `question_answer` (`question_id`, `answer`, `keywords`) VALUES
(@q_id8, 'TRUE', 'String,不可变,final,新对象');