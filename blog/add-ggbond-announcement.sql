-- 为GGBond(用户ID 15)添加空间公告
INSERT INTO announcements (user_id, content, created_at, updated_at)
VALUES (15, '欢迎来到我的个人空间!这里分享技术、生活和思考。', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    content = '欢迎来到我的个人空间!这里分享技术、生活和思考。',
    updated_at = NOW();
