-- 添加中文敏感词
-- 执行此脚本以添加常见的中文敏感词

INSERT INTO sensitive_words (word, created_at, updated_at) VALUES
-- 侮辱性词汇
('傻逼', NOW(), NOW()),
('傻B', NOW(), NOW()),
('傻比', NOW(), NOW()),
('白痴', NOW(), NOW()),
('智障', NOW(), NOW()),
('弱智', NOW(), NOW()),
('脑残', NOW(), NOW()),
('神经病', NOW(), NOW()),
('精神病', NOW(), NOW()),
('疯子', NOW(), NOW()),
('蠢货', NOW(), NOW()),
('废物', NOW(), NOW()),
('垃圾', NOW(), NOW()),
('人渣', NOW(), NOW()),
('畜生', NOW(), NOW()),
('贱人', NOW(), NOW()),
('婊子', NOW(), NOW()),
('妈的', NOW(), NOW()),
('他妈的', NOW(), NOW()),
('草泥马', NOW(), NOW()),
('操', NOW(), NOW()),
('艹', NOW(), NOW()),
('日', NOW(), NOW()),
('滚', NOW(), NOW()),
('滚蛋', NOW(), NOW()),
('去死', NOW(), NOW()),
('找死', NOW(), NOW()),

-- 色情相关
('色情', NOW(), NOW()),
('黄色', NOW(), NOW()),
('裸体', NOW(), NOW()),
('性交', NOW(), NOW()),
('做爱', NOW(), NOW()),
('强奸', NOW(), NOW()),
('淫荡', NOW(), NOW()),
('卖淫', NOW(), NOW()),
('嫖娼', NOW(), NOW()),

-- 暴力相关
('杀人', NOW(), NOW()),
('谋杀', NOW(), NOW()),
('自杀', NOW(), NOW()),
('炸弹', NOW(), NOW()),
('爆炸', NOW(), NOW()),
('恐怖', NOW(), NOW()),
('暴力', NOW(), NOW()),
('血腥', NOW(), NOW()),

-- 赌博相关
('赌博', NOW(), NOW()),
('赌场', NOW(), NOW()),
('赌钱', NOW(), NOW()),
('博彩', NOW(), NOW()),
('六合彩', NOW(), NOW()),
('时时彩', NOW(), NOW()),

-- 毒品相关
('毒品', NOW(), NOW()),
('吸毒', NOW(), NOW()),
('贩毒', NOW(), NOW()),
('海洛因', NOW(), NOW()),
('冰毒', NOW(), NOW()),
('大麻', NOW(), NOW()),
('摇头丸', NOW(), NOW()),

-- 诈骗相关
('诈骗', NOW(), NOW()),
('骗钱', NOW(), NOW()),
('传销', NOW(), NOW()),
('非法集资', NOW(), NOW()),
('洗钱', NOW(), NOW()),

-- 其他不当内容
('人肉搜索', NOW(), NOW()),
('泄露隐私', NOW(), NOW()),
('侵犯隐私', NOW(), NOW());

-- 查看添加结果
SELECT COUNT(*) AS '中文敏感词数量' FROM sensitive_words WHERE word REGEXP '[\\u4e00-\\u9fa5]';
SELECT '中文敏感词添加完成' AS message;
