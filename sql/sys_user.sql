CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密后的密码',
  `nickname` VARCHAR(64) NOT NULL COMMENT '显示昵称/考生姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：ADMIN/STUDENT',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删，1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 初始化管理员账号：admin / admin123
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`, `is_deleted`)
SELECT 'admin', '$2a$10$i0dGtR9I6ud9PmaxgO90ZODRdh8zmbfSNM0xI9.c024qQbWboBAvK', '系统管理员', 'ADMIN', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');

-- 初始化学生演示账号：student / student123
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`, `is_deleted`)
SELECT 'student', '$2a$10$9vS9aJkLapgaaazo73aXb.DLE111ReizHM2NcBXtdHZWPkaF2SiwK', '学生用户', 'STUDENT', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'student');
