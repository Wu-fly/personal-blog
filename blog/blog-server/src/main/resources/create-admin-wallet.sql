INSERT INTO users (phone, email, password, nickname, avatar, bio, role, status, created_at, updated_at) 
VALUES (
  '13900000000', 
  'admin@blog.com', 
  '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 
  'Admin', 
  'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 
  'System Administrator', 
  'ADMIN', 
  'ACTIVE', 
  NOW(), 
  NOW()
);

INSERT INTO wallets (user_id, balance, total_income, total_withdraw, created_at, updated_at)
SELECT id, 0.00, 0.00, 0.00, NOW(), NOW()
FROM users WHERE phone='13900000000';
