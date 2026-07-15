-- 使用后端生成的正确BCrypt哈希更新管理员密码
UPDATE users 
SET password = '$2a$10$Rv6K9tQWkvZTirKuT/z1d.yRzlFoUVKdSTpvN7CxrabvvcH41c/uK'
WHERE phone = '13900000000';

SELECT id, phone, email, nickname, role, LEFT(password, 30) as pwd_check 
FROM users 
WHERE phone = '13900000000';
