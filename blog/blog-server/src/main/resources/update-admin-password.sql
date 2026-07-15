-- 更新管理员密码为 admin123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE phone = '13900000000';

SELECT id, phone, email, nickname, role, LEFT(password, 20) as pwd_check 
FROM users 
WHERE phone = '13900000000';
