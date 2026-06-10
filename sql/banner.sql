CREATE TABLE `banners` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '轮播图描述',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_is_active`(`is_active` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

INSERT INTO `banners`
(`title`, `description`, `image_url`, `link_url`, `sort_order`, `is_active`, `is_deleted`)
VALUES
('AI赋能智慧学习',
 '通过人工智能技术辅助学习，为学生提供更高效、更个性化的学习体验。',
 'https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8YWklMjBlZHVjYXRpb258ZW58MHx8MHx8fDA%3D&ixlib=rb-4.1.0&q=60&w=3000',
 NULL, 1, 1, 0),

('智能学习助手上线',
 'AI助手可辅助知识问答、学习规划与内容推荐，让学习过程更加轻松。',
 'https://images.unsplash.com/photo-1677691824188-3e266886cb27?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8YWklMjBlZHVjYXRpb258ZW58MHx8MHx8fDA%3D&ixlib=rb-4.1.0&q=60&w=3000',
 NULL, 2, 1, 0),

('AI驱动个性化学习',
 '基于学习数据分析，智能生成学习路径，帮助学生精准提升薄弱知识点。',
 'https://images.unsplash.com/photo-1674027444485-cec3da58eef4?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8YXJ0aWZpY2lhbCUyMGludGVsbGlnZW5jZXxlbnwwfHwwfHx8MA%3D%3D&ixlib=rb-4.1.0&q=60&w=3000',
 NULL, 3, 1, 0),

('开启未来智能课堂',
 '结合人工智能、大数据与在线学习平台，打造更智能的数字化课堂。',
 'https://images.unsplash.com/photo-1677442135703-1787eea5ce01?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8YXJ0aWZpY2lhbCUyMGludGVsbGlnZW5jZXxlbnwwfHwwfHx8MA%3D%3D&ixlib=rb-4.1.0&q=60&w=3000',
 NULL, 4, 1, 0),

('AI智能学习新时代',
 '让知识获取更智能，让学习反馈更及时，让成长路径更加清晰。',
 'https://images.unsplash.com/photo-1734597949889-f8e2ec87c8ea?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTR8fGFydGlmaWNpYWwlMjBpbnRlbGxpZ2VuY2V8ZW58MHx8MHx8fDA%3D&ixlib=rb-4.1.0&q=60&w=3000',
 NULL, 5, 1, 0),

('智能问答辅助学习',
 'AI模型辅助答疑解惑，帮助用户快速理解知识点，提升学习效率。',
 'https://images.unsplash.com/photo-1677691824304-279660ceece3?auto=format&fit=crop&fm=jpg&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTJ8fGFpJTIwZWR1Y2F0aW9ufGVufDB8fDB8fHww&ixlib=rb-4.1.0&q=60&w=3000',
 NULL, 6, 1, 0);