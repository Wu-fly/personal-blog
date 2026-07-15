-- 更新分类数据,使其与首页一致
-- 删除旧的分类
DELETE FROM categories;

-- 插入新的分类
INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
(1, '技术开发', 'AI、云计算、编程技术等', NOW(), NOW()),
(2, '生活感悟', '美食、旅行、摄影等生活记录', NOW(), NOW()),
(3, '职场经验', '职业发展、技能提升等', NOW(), NOW()),
(4, '健康养生', '健身、营养、心理健康等', NOW(), NOW()),
(5, '读书笔记', '读书心得、知识分享等', NOW(), NOW()),
(6, '历史文化', '历史故事、文化传承等', NOW(), NOW()),
(7, '电子竞技', '游戏、电竞相关内容', NOW(), NOW());
