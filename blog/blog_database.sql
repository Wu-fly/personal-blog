-- =============================================
-- 个人博客系统数据库建表脚本
-- 适用于MySQL 5.7+ / MySQL 8.0+
-- 可在Navicat中直接执行
-- =============================================

-- 创建数据库（如果需要）
-- CREATE DATABASE IF NOT EXISTS blog_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE blog_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 表3.1 users(用户信息)表
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户的唯一标识符',
  `phone` VARCHAR(20) NOT NULL COMMENT '用户的手机号码',
  `email` VARCHAR(100) NOT NULL COMMENT '用户的电子邮箱地址',
  `password` VARCHAR(255) NOT NULL COMMENT '用户的密码(加密存储)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户的昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户的头像URL',
  `bio` TEXT DEFAULT NULL COMMENT '用户的个人简介',
  `role` ENUM('USER', 'BLOGGER', 'ADMIN') DEFAULT 'USER' COMMENT '用户角色',
  `status` ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE' COMMENT '账户状态',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账户更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- ----------------------------
-- 表3.3 categories(分类信息)表
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类的唯一标识符',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- ----------------------------
-- 表3.4 tags(标签信息)表
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '标签的唯一标识符',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';


-- ----------------------------
-- 表3.2 articles(文章信息)表
-- ----------------------------
DROP TABLE IF EXISTS `articles`;
CREATE TABLE `articles` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文章的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `content` LONGTEXT NOT NULL COMMENT '文章内容',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `category_id` BIGINT(20) DEFAULT NULL COMMENT '外键关联分类表',
  `is_paid` TINYINT(1) DEFAULT 0 COMMENT '是否付费文章(0否1是)',
  `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '文章价格',
  `is_pinned` TINYINT(1) DEFAULT 0 COMMENT '是否置顶(0否1是)',
  `pinned_at` TIMESTAMP NULL DEFAULT NULL COMMENT '置顶时间',
  `review_status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审核状态',
  `review_comment` TEXT DEFAULT NULL COMMENT '审核意见',
  `view_count` INT(11) DEFAULT 0 COMMENT '浏览量',
  `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
  `favorite_count` INT(11) DEFAULT 0 COMMENT '收藏数',
  `purchase_count` INT(11) DEFAULT 0 COMMENT '购买数',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `published_at` TIMESTAMP NULL DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_review_status` (`review_status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_articles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_articles_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章信息表';

-- ----------------------------
-- 表3.5 article_tags(文章标签关联)表
-- ----------------------------
DROP TABLE IF EXISTS `article_tags`;
CREATE TABLE `article_tags` (
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `tag_id` BIGINT(20) NOT NULL COMMENT '外键关联标签表',
  PRIMARY KEY (`article_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_article_tags_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_article_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- ----------------------------
-- 表3.6 comments(评论信息)表
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论的唯一标识符',
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父评论ID(用于嵌套回复)',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'APPROVED' COMMENT '评论状态',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_comments_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论信息表';


-- ----------------------------
-- 表3.7 likes(点赞信息)表
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞记录的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_likes_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞信息表';

-- ----------------------------
-- 表3.8 favorites(收藏信息)表
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '收藏记录的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorites_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏信息表';

-- ----------------------------
-- 表3.9 follows(关注信息)表
-- ----------------------------
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关注记录的唯一标识符',
  `follower_id` BIGINT(20) NOT NULL COMMENT '关注者ID(外键关联用户表)',
  `following_id` BIGINT(20) NOT NULL COMMENT '被关注者ID(外键关联用户表)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_following_id` (`following_id`),
  CONSTRAINT `fk_follows_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_follows_following` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注信息表';

-- ----------------------------
-- 表3.10 wallets(钱包信息)表
-- ----------------------------
DROP TABLE IF EXISTS `wallets`;
CREATE TABLE `wallets` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '钱包的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表(唯一)',
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '当前余额',
  `total_income` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总收入',
  `total_withdraw` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总提现',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_wallets_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包信息表';


-- ----------------------------
-- 表3.11 transactions(交易记录)表
-- ----------------------------
DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '交易记录的唯一标识符',
  `wallet_id` BIGINT(20) NOT NULL COMMENT '外键关联钱包表',
  `type` ENUM('RECHARGE', 'WITHDRAW', 'REWARD', 'PURCHASE', 'INCOME') NOT NULL COMMENT '交易类型',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
  `related_user_id` BIGINT(20) DEFAULT NULL COMMENT '关联用户ID',
  `related_article_id` BIGINT(20) DEFAULT NULL COMMENT '关联文章ID',
  `status` ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'SUCCESS' COMMENT '交易状态',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '交易描述',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
  PRIMARY KEY (`id`),
  KEY `idx_wallet_id` (`wallet_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_transactions_wallet` FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- ----------------------------
-- 表3.12 purchases(购买记录)表
-- ----------------------------
DROP TABLE IF EXISTS `purchases`;
CREATE TABLE `purchases` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '购买记录的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '购买金额',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  CONSTRAINT `fk_purchases_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_purchases_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购买记录表';

-- ----------------------------
-- 表3.13 messages(私信信息)表
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '私信的唯一标识符',
  `sender_id` BIGINT(20) NOT NULL COMMENT '发送者ID(外键关联用户表)',
  `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID(外键关联用户表)',
  `content` TEXT NOT NULL COMMENT '私信内容',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读(0未读1已读)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信信息表';

-- ----------------------------
-- 表3.14 browse_history(浏览历史)表
-- ----------------------------
DROP TABLE IF EXISTS `browse_history`;
CREATE TABLE `browse_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '浏览记录的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近浏览时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_updated_at` (`updated_at`),
  CONSTRAINT `fk_browse_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_browse_history_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';


-- ----------------------------
-- 表3.15 announcements(公告信息)表
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公告的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_announcements_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告信息表';

-- ----------------------------
-- 表3.16 carousel_config(轮播图配置)表
-- ----------------------------
DROP TABLE IF EXISTS `carousel_config`;
CREATE TABLE `carousel_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置的唯一标识符',
  `article_id` BIGINT(20) NOT NULL COMMENT '外键关联文章表',
  `display_order` INT(11) NOT NULL COMMENT '显示顺序',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_id` (`article_id`),
  KEY `idx_display_order` (`display_order`),
  CONSTRAINT `fk_carousel_config_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图配置表';

-- ----------------------------
-- 表3.17 blogger_applications(博主申请)表
-- ----------------------------
DROP TABLE IF EXISTS `blogger_applications`;
CREATE TABLE `blogger_applications` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '申请的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表',
  `nickname` VARCHAR(50) NOT NULL COMMENT '申请的博主昵称',
  `bio` TEXT DEFAULT NULL COMMENT '申请的博主简介',
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申请状态',
  `review_comment` TEXT DEFAULT NULL COMMENT '审核意见',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `reviewed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_blogger_applications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博主申请表';

-- ----------------------------
-- 表3.18 sensitive_words(敏感词)表
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '敏感词的唯一标识符',
  `word` VARCHAR(50) NOT NULL COMMENT '敏感词内容',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- ----------------------------
-- 表3.19 space_settings(个人空间设置)表
-- ----------------------------
DROP TABLE IF EXISTS `space_settings`;
CREATE TABLE `space_settings` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '设置的唯一标识符',
  `user_id` BIGINT(20) NOT NULL COMMENT '外键关联用户表(唯一)',
  `theme_color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '主题颜色',
  `background_image` VARCHAR(255) DEFAULT NULL COMMENT '背景图片URL',
  `layout_style` ENUM('SINGLE', 'DOUBLE', 'CARD') DEFAULT 'CARD' COMMENT '布局样式',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_space_settings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人空间设置表';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 初始化数据（可选）
-- =============================================

-- 插入默认分类
INSERT INTO `categories` (`name`, `description`) VALUES
('技术', '技术相关文章'),
('生活', '生活记录分享'),
('随笔', '随笔杂谈'),
('教程', '教程指南');

-- 插入默认标签
INSERT INTO `tags` (`name`) VALUES
('Java'),
('Spring Boot'),
('Vue.js'),
('MySQL'),
('前端'),
('后端'),
('全栈');

-- 插入管理员账号（密码为加密后的 Admin123456）
-- INSERT INTO `users` (`phone`, `email`, `password`, `nickname`, `role`, `status`) VALUES
-- ('13800000000', 'admin@blog.com', '$2a$10$xxxxx', '系统管理员', 'ADMIN', 'ACTIVE');

-- =============================================
-- 脚本执行完成
-- =============================================
