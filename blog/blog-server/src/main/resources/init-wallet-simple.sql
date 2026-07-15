-- 初始化钱包数据 - 简化版本
-- 为用户ID=3（胡雪岩）创建钱包和交易记录

-- 创建钱包
INSERT INTO wallets (user_id, balance, total_income, total_withdraw, created_at, updated_at)
VALUES (3, 1688.50, 3256.80, 500.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  balance = 1688.50,
  total_income = 3256.80,
  total_withdraw = 500.00;

-- 为其他用户创建钱包
INSERT INTO wallets (user_id, balance, total_income, total_withdraw, created_at, updated_at)
VALUES (4, 0.00, 0.00, 0.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO wallets (user_id, balance, total_income, total_withdraw, created_at, updated_at)
VALUES (5, 0.00, 0.00, 0.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE user_id = user_id;

INSERT INTO wallets (user_id, balance, total_income, total_withdraw, created_at, updated_at)
VALUES (6, 0.00, 0.00, 0.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 插入交易记录（使用子查询获取wallet_id）
INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'INCOME', 29.90, 1688.50, 5, 1, 'SUCCESS', '付费文章收益', '2025-12-25 10:30:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'REWARD', 50.00, 1593.70, 5, 1, 'SUCCESS', '收到打赏', '2025-12-24 18:45:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'PURCHASE', -19.90, 1655.70, 2, 11, 'SUCCESS', '购买付费文章', '2025-12-24 14:20:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, status, description, created_at)
SELECT id, 'RECHARGE', 200.00, 1455.70, 'SUCCESS', '钱包充值', '2025-12-23 09:00:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'INCOME', 39.90, 1658.60, 6, 3, 'SUCCESS', '付费文章收益', '2025-12-22 16:30:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, status, description, created_at)
SELECT id, 'WITHDRAW', -500.00, 1155.70, 'SUCCESS', '提现', '2025-12-21 11:00:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'REWARD', 88.00, 1543.70, 6, 4, 'SUCCESS', '收到打赏', '2025-12-20 20:15:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'INCOME', 25.00, 1618.70, 7, 4, 'SUCCESS', '付费文章收益', '2025-12-19 13:40:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, related_user_id, related_article_id, status, description, created_at)
SELECT id, 'PURCHASE', -15.00, 1675.60, 3, 13, 'SUCCESS', '购买付费文章', '2025-12-18 17:25:00' FROM wallets WHERE user_id = 3;

INSERT INTO transactions (wallet_id, type, amount, balance_after, status, description, created_at)
SELECT id, 'RECHARGE', 100.00, 1255.70, 'SUCCESS', '钱包充值', '2025-12-17 08:30:00' FROM wallets WHERE user_id = 3;
