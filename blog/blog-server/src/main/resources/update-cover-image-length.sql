-- 增加articles表的cover_image字段长度
-- 从VARCHAR(255)改为VARCHAR(500)以支持更长的URL

USE blog_database;

ALTER TABLE articles MODIFY COLUMN cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL';

-- 验证修改
DESCRIBE articles;
