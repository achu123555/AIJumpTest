CREATE TABLE `answer_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `exam_record_id` int NOT NULL COMMENT '考试记录ID，关联 exam_records 表',
  `question_id` bigint NOT NULL COMMENT '题目ID，关联 questions 表',
  `user_answer` varchar(1000) DEFAULT NULL COMMENT '用户提交的答案（单选/多选/判断/简答文本）',
  `score` int DEFAULT '0' COMMENT '本题实际得分',
  `is_correct` tinyint DEFAULT '0' COMMENT '是否正确（0:错误，1:正确）',
  `ai_correction` text COMMENT 'AI对该题的详细批阅/批改意见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0:未删除，1:已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_exam_record_id` (`exam_record_id`),
  KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='答题详情记录表';