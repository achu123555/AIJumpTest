CREATE TABLE `exam_records`
(
    `id`              int         NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `paper_id`        int         NOT NULL COMMENT '试卷ID。关联的考试试卷',
    `student_name`    varchar(50) NOT NULL COMMENT '考生姓名',
    `score`           int         DEFAULT '0' COMMENT '考试得分',
    `appraisal`       text COMMENT 'AI评价记录，存储AI对该考试试卷的评价内容',
    `start_time`      datetime    DEFAULT NULL COMMENT '考试开始时间',
    `end_time`        datetime    DEFAULT NULL COMMENT '考试结束时间',
    `status`          varchar(20) DEFAULT '进行中' COMMENT '考试状态（进行中、已完成、已批阅）',
    `window_switches` int         DEFAULT '0' COMMENT '窗口切换次数，用于监控考试过程中的异常行为',
    `create_time`     datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint     DEFAULT '0' COMMENT '逻辑删除（0:未删除，1:已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_paper_id` (`paper_id`),
    KEY `idx_student_name` (`student_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='考试记录信息表';