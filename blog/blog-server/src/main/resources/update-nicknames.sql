-- 更新用户昵称为中文名字，与博客前端显示一致
USE blog_system;

-- 更新博主昵称和简介
UPDATE users SET 
  nickname = '胡雪岩', 
  bio = '红顶商人，徽商代表，一代商圣。经商之道，在于诚信为本，以义取利。',
  avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan'
WHERE email = 'tech.blogger@blog.com';

UPDATE users SET 
  nickname = '左宗棠', 
  bio = '晚清名臣，收复新疆，创办福建船政局。',
  avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang'
WHERE email = 'life.blogger@blog.com';

UPDATE users SET 
  nickname = '盛宣怀', 
  bio = '洋务运动代表，创办轮船招商局、北洋大学堂。',
  avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai'
WHERE email = 'edu.blogger@blog.com';

UPDATE users SET 
  nickname = '张謇', 
  bio = '状元实业家，创办大生纱厂，实业救国的先驱。',
  avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian'
WHERE email = 'health.blogger@blog.com';

UPDATE users SET 
  nickname = '李鸿章', 
  bio = '晚清重臣，洋务运动领袖，外交家。',
  avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=lihongzhang'
WHERE email = 'entertainment.blogger@blog.com';

-- 验证更新结果
SELECT id, nickname, email, role FROM users WHERE role IN ('BLOGGER', 'ADMIN') ORDER BY id;
