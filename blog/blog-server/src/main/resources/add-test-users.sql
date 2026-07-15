-- 添加测试博主用户
-- 密码都是 123456

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('huxueyan', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '胡雪岩', 'huxueyan@example.com', '13800138001', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('zuozongtang', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '左宗棠', 'zuozongtang@example.com', '13800138002', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('shengxuanhuai', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '盛宣怀', 'shengxuanhuai@example.com', '13800138003', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('zhangjian', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '张謇', 'zhangjian@example.com', '13800138004', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('lihongzhang', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '李鸿章', 'lihongzhang@example.com', '13800138005', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('zengguofan', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '曾国藩', 'zengguofan@example.com', '13800138006', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('linzexu', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '林则徐', 'linzexu@example.com', '13800138007', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

INSERT INTO user (username, password, nickname, email, phone, role, status, is_blogger, created_at, updated_at) 
VALUES ('kangyw', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', '康有为', 'kangyw@example.com', '13800138008', 'BLOGGER', 'ACTIVE', 1, NOW(), NOW());

SELECT '用户添加完成' AS message;
SELECT COUNT(*) AS blogger_count FROM user WHERE is_blogger = 1;
