-- ============================================
-- 数据库索引优化 SQL 脚本
-- 个人博客系统
-- ============================================

-- 使用数据库
USE blog_system;

-- ============================================
-- 1. 文章表 (articles) 索引优化
-- ============================================

-- 复合索引：用于筛选和排序（按创建时间）
CREATE INDEX IF NOT EXISTS idx_articles_status_category_created 
ON articles(review_status, category_id, created_at DESC);

-- 复合索引：用于按浏览量排序
CREATE INDEX IF NOT EXISTS idx_articles_status_view_count 
ON articles(review_status, view_count DESC);

-- 复合索引：用于按收藏数排序
CREATE INDEX IF NOT EXISTS idx_articles_status_favorite_count 
ON articles(review_status, favorite_count DESC);

-- 复合索引：用于按购买数排序
CREATE INDEX IF NOT EXISTS idx_articles_status_purchase_count 
ON articles(review_status, purchase_count DESC);

-- 复合索引：用于博主个人空间（包含置顶文章）
CREATE INDEX IF NOT EXISTS idx_articles_user_status_pinned 
ON articles(user_id, review_status, is_pinned DESC, pinned_at DESC);

-- 复合索引：用于付费文章查询
CREATE INDEX IF NOT EXISTS idx_articles_status_paid_price 
ON articles(review_status, is_paid, price);

-- ============================================
-- 2. 评论表 (comments) 索引优化
-- ============================================

-- 复合索引：用于文章评论查询（包含父评论过滤）
CREATE INDEX IF NOT EXISTS idx_comments_article_parent_created 
ON comments(article_id, parent_id, created_at DESC);

-- 复合索引：用于评论审核查询
CREATE INDEX IF NOT EXISTS idx_comments_status_created 
ON comments(status, created_at DESC);

-- ============================================
-- 3. 浏览历史表 (browse_history) 索引优化
-- ============================================

-- 复合索引：用于用户浏览历史查询
CREATE INDEX IF NOT EXISTS idx_browse_history_user_updated 
ON browse_history(user_id, updated_at DESC);

-- 复合索引：用于查找特定用户和文章的浏览记录
CREATE INDEX IF NOT EXISTS idx_browse_history_user_article 
ON browse_history(user_id, article_id);

-- ============================================
-- 4. 收藏表 (favorites) 索引优化
-- ============================================

-- 复合索引：用于用户收藏列表查询
CREATE INDEX IF NOT EXISTS idx_favorites_user_created 
ON favorites(user_id, created_at DESC);

-- 复合索引：用于文章收藏统计
CREATE INDEX IF NOT EXISTS idx_favorites_article_created 
ON favorites(article_id, created_at DESC);

-- ============================================
-- 5. 点赞表 (likes) 索引优化
-- ============================================

-- 复合索引：用于用户点赞列表查询
CREATE INDEX IF NOT EXISTS idx_likes_user_created 
ON likes(user_id, created_at DESC);

-- 复合索引：用于文章点赞统计
CREATE INDEX IF NOT EXISTS idx_likes_article_created 
ON likes(article_id, created_at DESC);

-- ============================================
-- 6. 关注表 (follows) 索引优化
-- ============================================

-- 复合索引：用于查询用户关注列表
CREATE INDEX IF NOT EXISTS idx_follows_follower_created 
ON follows(follower_id, created_at DESC);

-- 复合索引：用于查询用户粉丝列表
CREATE INDEX IF NOT EXISTS idx_follows_following_created 
ON follows(following_id, created_at DESC);

-- ============================================
-- 7. 私信表 (messages) 索引优化
-- ============================================

-- 复合索引：用于发送者-接收者对话查询
CREATE INDEX IF NOT EXISTS idx_messages_sender_receiver_created 
ON messages(sender_id, receiver_id, created_at ASC);

-- 复合索引：用于接收者-发送者对话查询
CREATE INDEX IF NOT EXISTS idx_messages_receiver_sender_created 
ON messages(receiver_id, sender_id, created_at ASC);

-- 复合索引：用于未读消息查询
CREATE INDEX IF NOT EXISTS idx_messages_receiver_read_created 
ON messages(receiver_id, is_read, created_at DESC);

-- ============================================
-- 8. 交易记录表 (transactions) 索引优化
-- ============================================

-- 复合索引：用于钱包交易记录查询（按类型筛选）
CREATE INDEX IF NOT EXISTS idx_transactions_wallet_type_created 
ON transactions(wallet_id, type, created_at DESC);

-- 复合索引：用于交易状态查询
CREATE INDEX IF NOT EXISTS idx_transactions_wallet_status_created 
ON transactions(wallet_id, status, created_at DESC);

-- ============================================
-- 9. 购买记录表 (purchases) 索引优化
-- ============================================

-- 复合索引：用于用户购买记录查询
CREATE INDEX IF NOT EXISTS idx_purchases_user_created 
ON purchases(user_id, created_at DESC);

-- 复合索引：用于文章购买统计
CREATE INDEX IF NOT EXISTS idx_purchases_article_created 
ON purchases(article_id, created_at DESC);

-- ============================================
-- 10. 博主申请表 (blogger_applications) 索引优化
-- ============================================

-- 复合索引：用于申请状态查询
CREATE INDEX IF NOT EXISTS idx_blogger_apps_status_created 
ON blogger_applications(status, created_at DESC);

-- ============================================
-- 11. 文章标签关联表 (article_tags) 索引优化
-- ============================================

-- 复合索引：用于标签文章查询
CREATE INDEX IF NOT EXISTS idx_article_tags_tag_article 
ON article_tags(tag_id, article_id);

-- ============================================
-- 12. 轮播图配置表 (carousel_config) 索引优化
-- ============================================

-- 索引：用于轮播图排序
CREATE INDEX IF NOT EXISTS idx_carousel_order 
ON carousel_config(display_order ASC);

-- ============================================
-- 索引使用情况分析查询
-- ============================================

-- 查看所有索引
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    CARDINALITY,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'blog_system'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 查看未使用的索引（需要启用 performance_schema）
-- SELECT 
--     object_schema,
--     object_name,
--     index_name
-- FROM performance_schema.table_io_waits_summary_by_index_usage
-- WHERE index_name IS NOT NULL
--     AND count_star = 0
--     AND object_schema = 'blog_system'
--     AND index_name != 'PRIMARY'
-- ORDER BY object_schema, object_name;

-- 查看索引选择性（选择性越高越好，接近 1 最好）
SELECT 
    s.TABLE_NAME,
    s.INDEX_NAME,
    s.CARDINALITY,
    t.TABLE_ROWS,
    ROUND(s.CARDINALITY / t.TABLE_ROWS, 4) AS selectivity
FROM information_schema.STATISTICS s
JOIN information_schema.TABLES t 
    ON s.TABLE_SCHEMA = t.TABLE_SCHEMA 
    AND s.TABLE_NAME = t.TABLE_NAME
WHERE s.TABLE_SCHEMA = 'blog_system'
    AND s.SEQ_IN_INDEX = 1
    AND s.INDEX_NAME != 'PRIMARY'
    AND t.TABLE_ROWS > 0
ORDER BY selectivity DESC;

-- ============================================
-- 查询性能分析示例
-- ============================================

-- 分析文章列表查询
EXPLAIN SELECT * FROM articles 
WHERE review_status = 'APPROVED' 
ORDER BY created_at DESC 
LIMIT 20;

-- 分析文章搜索查询
EXPLAIN SELECT * FROM articles 
WHERE review_status = 'APPROVED' 
    AND (title LIKE '%关键词%' OR content LIKE '%关键词%')
ORDER BY created_at DESC 
LIMIT 20;

-- 分析评论查询
EXPLAIN SELECT * FROM comments 
WHERE article_id = 1 
    AND parent_id IS NULL 
ORDER BY created_at DESC 
LIMIT 20;

-- 分析浏览历史查询
EXPLAIN SELECT * FROM browse_history 
WHERE user_id = 1 
ORDER BY updated_at DESC 
LIMIT 100;

-- ============================================
-- 索引维护
-- ============================================

-- 分析表（更新索引统计信息）
ANALYZE TABLE articles;
ANALYZE TABLE comments;
ANALYZE TABLE favorites;
ANALYZE TABLE likes;
ANALYZE TABLE browse_history;
ANALYZE TABLE messages;
ANALYZE TABLE transactions;

-- 优化表（重建索引，回收空间）
-- 注意：在生产环境执行时需要在低峰期进行
-- OPTIMIZE TABLE articles;
-- OPTIMIZE TABLE comments;
-- OPTIMIZE TABLE favorites;
-- OPTIMIZE TABLE likes;

-- ============================================
-- 完成
-- ============================================

SELECT 'Database index optimization completed!' AS status;
