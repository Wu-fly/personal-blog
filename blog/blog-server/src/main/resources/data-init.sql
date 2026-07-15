-- ========================================
-- Personal Blog System Initialization Data Script
-- Version: 1.0
-- Created: 2025-01-01
-- Description: Includes admin account, blogger accounts, categories, tags, sample articles and sensitive words
-- ========================================

-- ========================================
-- Task 50.1: Create Data Initialization Script
-- ========================================

-- 1. Create admin account
-- Password: admin123 (BCrypt encrypted)
-- Note: Admin account already created, skipping here
-- INSERT INTO users (phone, email, password, nickname, avatar, bio, role, status, created_at, updated_at) VALUES
-- ('13900000000', 'admin@blog.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 'Admin', 
-- 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'System Administrator', 'ADMIN', 'ACTIVE', NOW(), NOW());

-- 2. Create sample blogger accounts
-- Password: blogger123 (BCrypt encrypted)
INSERT IGNORE INTO users (phone, email, password, nickname, avatar, bio, role, status, created_at, updated_at) VALUES
('13800000001', 'tech.blogger@blog.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 'Tech Explorer', 
'https://api.dicebear.com/7.x/avataaars/svg?seed=tech', 'AI and Cloud Computing', 'BLOGGER', 'ACTIVE', NOW(), NOW()),

('13800000002', 'life.blogger@blog.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 'Life Artist', 
'https://api.dicebear.com/7.x/avataaars/svg?seed=life', 'Sharing life moments', 'BLOGGER', 'ACTIVE', NOW(), NOW()),

('13800000003', 'edu.blogger@blog.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 'Edu Pioneer', 
'https://api.dicebear.com/7.x/avataaars/svg?seed=edu', 'Education innovation', 'BLOGGER', 'ACTIVE', NOW(), NOW()),

('13800000004', 'health.blogger@blog.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 'Health Guru', 
'https://api.dicebear.com/7.x/avataaars/svg?seed=health', 'Healthy lifestyle', 'BLOGGER', 'ACTIVE', NOW(), NOW()),

('13800000005', 'entertainment.blogger@blog.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQopqeyb00W9Yzn9Mf3CvGn4Gy', 'Entertainment', 
'https://api.dicebear.com/7.x/avataaars/svg?seed=entertainment', 'Entertainment news', 'BLOGGER', 'ACTIVE', NOW(), NOW());

-- 3. Create wallets for bloggers
INSERT IGNORE INTO wallets (user_id, balance, total_income, total_withdraw, created_at, updated_at)
SELECT id, 0.00, 0.00, 0.00, NOW(), NOW()
FROM users WHERE role IN ('BLOGGER', 'ADMIN');

-- 4. Create article categories
INSERT INTO categories (name, description, created_at, updated_at) VALUES
('技术开发', 'AI、云计算、编程技术等', NOW(), NOW()),
('生活感悟', '美食、旅行、摄影等生活记录', NOW(), NOW()),
('职场经验', '职业发展、技能提升等', NOW(), NOW()),
('健康养生', '健身、营养、心理健康等', NOW(), NOW()),
('读书笔记', '读书心得、知识分享等', NOW(), NOW()),
('历史文化', '历史故事、文化传承等', NOW(), NOW()),
('电子竞技', '游戏、电竞相关内容', NOW(), NOW());

-- 5. Create sample tags
INSERT INTO tags (name, created_at, updated_at) VALUES
-- Tech tags
('AI', NOW(), NOW()),
('ChatGPT', NOW(), NOW()),
('LLM', NOW(), NOW()),
('Cloud', NOW(), NOW()),
('Quantum', NOW(), NOW()),
('5G', NOW(), NOW()),
('IoT', NOW(), NOW()),
('Blockchain', NOW(), NOW()),
('BCI', NOW(), NOW()),
-- Life tags
('Lifestyle', NOW(), NOW()),
('Food', NOW(), NOW()),
('Travel', NOW(), NOW()),
('Photography', NOW(), NOW()),
('Fashion', NOW(), NOW()),
('SmartHome', NOW(), NOW()),
-- Entertainment tags
('Movies', NOW(), NOW()),
('Music', NOW(), NOW()),
('Games', NOW(), NOW()),
('Anime', NOW(), NOW()),
('VirtualIdol', NOW(), NOW()),
-- Education tags
('OnlineLearning', NOW(), NOW()),
('Career', NOW(), NOW()),
('Skills', NOW(), NOW()),
('Reading', NOW(), NOW()),
('Metaverse', NOW(), NOW()),
-- Health tags
('Wellness', NOW(), NOW()),
('Fitness', NOW(), NOW()),
('MentalHealth', NOW(), NOW()),
('Nutrition', NOW(), NOW()),
('AntiAging', NOW(), NOW());

-- ========================================
-- Task 50.2: Import Sample Articles (2025 Hot Topics)
-- ========================================

-- Tech Articles (Blogger 1: Tech Explorer)
INSERT INTO articles (user_id, title, content, summary, cover_image, category_id, is_paid, price, 
                      review_status, view_count, like_count, favorite_count, purchase_count, 
                      published_at, created_at, updated_at) VALUES

-- Article 1: AI LLM
((SELECT id FROM users WHERE email = 'tech.blogger@blog.com'),
'GPT-5 Breakthrough: AI Enters New Era in 2025',
'<h2>Introduction</h2><p>2025 marks a historic breakthrough in AI. GPT-5 launch signals a new stage for large language models...</p><h2>Technical Advances</h2><p>GPT-5 shows significant improvements in multimodal understanding, reasoning, and creativity. With 10x more parameters than GPT-4, reaching 10 trillion...</p><h2>Applications</h2><p>From medical diagnosis to scientific research, from education to creative design, GPT-5 is transforming industries...</p><h2>Future Outlook</h2><p>AI will integrate deeper into our daily lives...</p>',
'GPT-5 breakthrough in 2025 leads AI into new era. Deep analysis of technical innovations and application prospects.',
'https://picsum.photos/800/450?random=1',
(SELECT id FROM categories WHERE name = '技术开发'),
true, 29.90, 'APPROVED', 15680, 2340, 1560, 156,
'2025-01-15 10:30:00', '2025-01-15 10:00:00', NOW()),

-- Article 2: Quantum Computing
((SELECT id FROM users WHERE email = 'tech.blogger@blog.com'),
'IBM Quantum Chip Breaks 1000 Qubit Barrier',
'<h2>Quantum Milestone</h2><p>March 2025, IBM announces first quantum chip exceeding 1000 qubits, marking commercial quantum computing era...</p><h2>Technical Principles</h2><p>Quantum computing leverages superposition and entanglement for exponential speedup on specific problems...</p><h2>Applications</h2><p>Drug discovery, financial modeling, cryptography, climate simulation will benefit first...</p>',
'IBM quantum chip exceeds 1000 qubits. Commercial quantum computing arrives. Deep dive into breakthrough and industry impact.',
'https://picsum.photos/800/450?random=2',
(SELECT id FROM categories WHERE name = '技术开发'),
false, 0, 'APPROVED', 12450, 1890, 980, 0,
'2025-03-20 14:20:00', '2025-03-20 14:00:00', NOW()),

-- Article 3: 6G Technology
((SELECT id FROM users WHERE email = 'tech.blogger@blog.com'),
'6G Standard Released: 100x Faster Communication',
'<h2>6G Era Arrives</h2><p>June 2025, ITU releases 6G standard with peak speed of 1Tbps, 100x faster than 5G...</p><h2>Core Tech</h2><p>Terahertz communication, intelligent surfaces, space-air-ground networks enable 6G...</p><h2>Future Apps</h2><p>Holographic communication, digital twins, brain-computer interfaces on 6G networks...</p>',
'6G standard released with 100x speed boost. Exploring unlimited possibilities of future communication technology.',
'https://picsum.photos/800/450?random=3',
(SELECT id FROM categories WHERE name = '技术开发'),
true, 19.90, 'APPROVED', 9870, 1450, 780, 87,
'2025-06-10 09:15:00', '2025-06-10 09:00:00', NOW()),

-- Article 4: Brain-Computer Interface
((SELECT id FROM users WHERE email = 'tech.blogger@blog.com'),
'Neuralink BCI Trial Success: Mind Control Era Begins',
'<h2>Breakthrough</h2><p>August 2025, Neuralink announces major BCI clinical trial success. First patients control computers and robotic arms with thoughts...</p><h2>Technical Details</h2><p>Ultra-thin flexible electrodes safely implanted in brain cortex, reading neural signals in real-time...</p><h2>Medical Applications</h2><p>New hope for paralyzed and ALS patients...</p>',
'Neuralink BCI trial achieves breakthrough success. Mind control technology from sci-fi to reality. Human-machine fusion begins.',
'https://picsum.photos/800/450?random=4',
(SELECT id FROM categories WHERE name = '技术开发'),
false, 0, 'APPROVED', 18900, 2560, 1780, 0,
'2025-08-05 16:40:00', '2025-08-05 16:20:00', NOW());

-- Lifestyle Articles (Blogger 2: Life Artist)
INSERT INTO articles (user_id, title, content, summary, cover_image, category_id, is_paid, price, 
                      review_status, view_count, like_count, favorite_count, purchase_count, 
                      published_at, created_at, updated_at) VALUES

-- Article 5: Sustainable Living
((SELECT id FROM users WHERE email = 'life.blogger@blog.com'),
'2025 Sustainable Living Guide: Zero Waste to Carbon Neutral',
'<h2>Importance</h2><p>As climate change intensifies, more people embrace sustainable living in 2025...</p><h2>Zero Waste</h2><p>Reduce single-use items, choose reusable products, learn recycling and composting...</p><h2>Carbon Neutral</h2><p>Calculate personal carbon footprint, choose green transportation, support renewable energy...</p><h2>Practical Tips</h2><p>Start with daily habits, everyone can contribute...</p>',
'Explore 2025 sustainable living from zero waste to carbon neutral. Share practical eco tips for green daily life.',
'https://picsum.photos/800/450?random=5',
(SELECT id FROM categories WHERE name = '生活感悟'),
false, 0, 'APPROVED', 8760, 1230, 760, 0,
'2025-02-14 11:30:00', '2025-02-14 11:00:00', NOW()),

-- Article 6: Smart Home
((SELECT id FROM users WHERE email = 'life.blogger@blog.com'),
'Building 2025 Smart Home: AI Butler Makes Life Better',
'<h2>Smart Home Trends</h2><p>2025 smart homes go beyond voice control to true AI butlers...</p><h2>Core Devices</h2><p>Smart speakers, locks, lighting, climate systems seamlessly coordinated...</p><h2>Scene Automation</h2><p>Wake mode, away mode, home mode, sleep mode smart scenes...</p><h2>Privacy Protection</h2><p>How to protect privacy while enjoying convenience...</p>',
'Deep dive into 2025 smart home life. How AI butler makes home smarter, more comfortable, safer. Detailed buying guide included.',
'https://picsum.photos/800/450?random=6',
(SELECT id FROM categories WHERE name = '生活感悟'),
true, 15.90, 'APPROVED', 7650, 1120, 680, 72,
'2025-04-22 15:45:00', '2025-04-22 15:30:00', NOW()),

-- Article 7: Food Trends
((SELECT id FROM users WHERE email = 'life.blogger@blog.com'),
'2025 Food Trends: Plant-Based and Cultured Meat Revolution',
'<h2>Food Revolution</h2><p>2025 sees plant-based and cultured meat become dining staples...</p><h2>Plant-Based Tech</h2><p>Plant protein restructuring perfectly mimics meat texture and nutrition...</p><h2>Cultured Meat</h2><p>Lab-grown real meat cells without animal slaughter...</p><h2>Taste Experience</h2><p>Trying future foods at multiple restaurants, sharing real taste...</p>',
'Explore 2025 food trends. How plant-based and cultured meat change eating habits. Delicious restaurant recommendations included.',
'https://picsum.photos/800/450?random=7',
(SELECT id FROM categories WHERE name = '生活感悟'),
false, 0, 'APPROVED', 6540, 980, 560, 0,
'2025-07-18 13:20:00', '2025-07-18 13:00:00', NOW()),

-- Article 8: Space Tourism
((SELECT id FROM users WHERE email = 'life.blogger@blog.com'),
'My First Space Tourism: 7 Days on ISS',
'<h2>Dream Come True</h2><p>September 2025, I finally realized my childhood dream, boarding spacecraft to ISS...</p><h2>Zero Gravity</h2><p>Amazing feeling of living in zero gravity, from eating to sleeping all new experiences...</p><h2>Earth Beauty</h2><p>Viewing Earth from space, that shock beyond words...</p><h2>Space Life</h2><p>Unforgettable experience working and living with astronauts...</p>',
'Real space tourism experience. Sharing 7 days on ISS from zero gravity to Earth views. Dreams become reality.',
'https://picsum.photos/800/450?random=8',
(SELECT id FROM categories WHERE name = '生活感悟'),
true, 49.90, 'APPROVED', 22340, 3120, 2180, 245,
'2025-09-25 10:10:00', '2025-09-25 10:00:00', NOW());

-- Education Articles (Blogger 3: Edu Pioneer)
INSERT INTO articles (user_id, title, content, summary, cover_image, category_id, is_paid, price, 
                      review_status, view_count, like_count, favorite_count, purchase_count, 
                      published_at, created_at, updated_at) VALUES

-- Article 9: AI Education
((SELECT id FROM users WHERE email = 'edu.blogger@blog.com'),
'AI Personalized Education: Every Child Has AI Tutor',
'<h2>Education Revolution</h2><p>2025 sees AI personalized education globally adopted. Every student has dedicated AI tutor...</p><h2>Personalized Learning</h2><p>AI creates custom learning plans based on student progress, interests, and abilities...</p><h2>Real-time Feedback</h2><p>Instantly identify learning issues, provide targeted guidance...</p><h2>Education Equity</h2><p>AI education makes quality resources accessible to all...</p>',
'Explore 2025 AI personalized education revolution. Every child can have dedicated AI tutor for true individualized teaching.',
'https://picsum.photos/800/450?random=9',
(SELECT id FROM categories WHERE name = '读书笔记'),
false, 0, 'APPROVED', 11230, 1560, 980, 0,
'2025-03-08 09:30:00', '2025-03-08 09:00:00', NOW()),

-- Article 10: Metaverse Education
((SELECT id FROM users WHERE email = 'edu.blogger@blog.com'),
'Metaverse Classroom: Immersive Learning Future Arrives',
'<h2>Virtual Classroom</h2><p>Put on VR headset, instantly enter Roman Colosseum, experience history firsthand...</p><h2>Interactive Learning</h2><p>Conduct chemistry experiments in virtual lab, safe and efficient...</p><h2>Global Collaboration</h2><p>Complete projects with students worldwide, breaking geographical barriers...</p><h2>Learning Effectiveness</h2><p>Immersive learning makes knowledge retention deeper...</p>',
'Experience metaverse classroom charm. How immersive learning changes education, making learning more fun and efficient.',
'https://picsum.photos/800/450?random=10',
(SELECT id FROM categories WHERE name = '读书笔记'),
true, 24.90, 'APPROVED', 9870, 1340, 870, 76,
'2025-05-12 14:50:00', '2025-05-12 14:30:00', NOW()),

-- Article 11: Lifelong Learning
((SELECT id FROM users WHERE email = 'edu.blogger@blog.com'),
'2025 Lifelong Learning Guide: Stay Competitive in AI Era',
'<h2>Necessity</h2><p>In rapidly changing AI era, lifelong learning is no longer optional but essential...</p><h2>Learning Strategies</h2><p>How to efficiently learn new skills, maintain career competitiveness...</p><h2>Online Resources</h2><p>Recommend quality online learning platforms and courses...</p><h2>Learning Communities</h2><p>Join learning communities, grow with like-minded people...</p>',
'Complete 2025 lifelong learning guide. Share strategies and practical resources for staying competitive in AI era.',
'https://picsum.photos/800/450?random=11',
(SELECT id FROM categories WHERE name = '读书笔记'),
false, 0, 'APPROVED', 8450, 1180, 720, 0,
'2025-07-30 11:40:00', '2025-07-30 11:20:00', NOW()),

-- Article 12: Career Education
((SELECT id FROM users WHERE email = 'edu.blogger@blog.com'),
'Top 10 Emerging Careers in 2025 and Learning Paths',
'<h2>Emerging Trends</h2><p>AI prompt engineer, digital twin architect, metaverse designer and other new careers rise...</p><h2>Skill Requirements</h2><p>Detailed analysis of core skills needed for each career...</p><h2>Learning Paths</h2><p>Complete learning roadmap from beginner to professional...</p><h2>Salary Outlook</h2><p>Salary levels and development prospects for each career...</p>',
'Top 10 hottest emerging careers in 2025. Detailed skill requirements and learning paths to seize career opportunities.',
'https://picsum.photos/800/450?random=12',
(SELECT id FROM categories WHERE name = '职场经验'),
true, 34.90, 'APPROVED', 16780, 2240, 1560, 168,
'2025-10-15 16:20:00', '2025-10-15 16:00:00', NOW());

-- Health Articles (Blogger 4: Health Guru)
INSERT INTO articles (user_id, title, content, summary, cover_image, category_id, is_paid, price, 
                      review_status, view_count, like_count, favorite_count, purchase_count, 
                      published_at, created_at, updated_at) VALUES

-- Article 13: Gene Editing
((SELECT id FROM users WHERE email = 'health.blogger@blog.com'),
'Gene Editing Therapy Approved: Cancer Treatment Enters Precision Era',
'<h2>Medical Breakthrough</h2><p>2025 sees multiple gene editing therapies FDA approved, bringing new hope to cancer patients...</p><h2>CRISPR Technology</h2><p>Precisely edit disease genes, treat diseases at the source...</p><h2>Clinical Applications</h2><p>Successfully treated multiple genetic diseases and cancers...</p><h2>Ethical Considerations</h2><p>Where are the boundaries of gene editing applications...</p>',
'Gene editing therapy approved. Cancer treatment enters precision medicine era. Deep dive into technology and clinical applications.',
'https://picsum.photos/800/450?random=13',
(SELECT id FROM categories WHERE name = '健康养生'),
false, 0, 'APPROVED', 13450, 1890, 1120, 0,
'2025-02-28 10:15:00', '2025-02-28 10:00:00', NOW()),

-- Article 14: Longevity Science
((SELECT id FROM users WHERE email = 'health.blogger@blog.com'),
'2025 Anti-Aging Breakthrough: NAD+ Therapy Rejuvenates Cells',
'<h2>Aging Secrets</h2><p>Scientists discover NAD+ decline is key factor in aging...</p><h2>NAD+ Therapy</h2><p>Supplement NAD+ precursors to activate longevity genes...</p><h2>Clinical Results</h2><p>Multiple studies show significant anti-aging effects...</p><h2>Practice Guide</h2><p>How to scientifically implement anti-aging interventions...</p>',
'Unveiling 2025 anti-aging breakthrough. How NAD+ therapy rejuvenates cells. Scientific anti-aging practice guide included.',
'https://picsum.photos/800/450?random=14',
(SELECT id FROM categories WHERE name = '健康养生'),
true, 39.90, 'APPROVED', 19870, 2780, 1980, 198,
'2025-04-10 15:30:00', '2025-04-10 15:00:00', NOW()),

-- Article 15: Mental Health
((SELECT id FROM users WHERE email = 'health.blogger@blog.com'),
'AI Therapist: 24/7 Mental Health Guardian',
'<h2>Mental Health Crisis</h2><p>Modern society stress increases, mental health issues become prominent...</p><h2>AI Therapy</h2><p>LLM-based AI therapist provides professional mental support...</p><h2>Privacy Protection</h2><p>Completely anonymous, no privacy concerns...</p><h2>User Experience</h2><p>Real user feedback and effectiveness reviews...</p>',
'Explore AI therapy services. 24/7 mental health guardian. Share user experience and professional advice.',
'https://picsum.photos/800/450?random=15',
(SELECT id FROM categories WHERE name = '健康养生'),
false, 0, 'APPROVED', 10230, 1450, 890, 0,
'2025-06-25 09:45:00', '2025-06-25 09:30:00', NOW()),

-- Article 16: Fitness Tech
((SELECT id FROM users WHERE email = 'health.blogger@blog.com'),
'Smart Fitness Mirror Review: Personal Trainer at Home',
'<h2>Smart Fitness Experience</h2><p>2025 smart fitness mirrors evolved to new heights...</p><h2>AI Personal Trainer</h2><p>Real-time motion correction, personalized training plans...</p><h2>Product Comparison</h2><p>Detailed comparison of mainstream smart fitness mirrors...</p><h2>Usage Experience</h2><p>Three months experience and fitness results sharing...</p>',
'In-depth review of 2025 smart fitness mirrors. How AI personal trainer makes home fitness more efficient. Detailed product comparison.',
'https://picsum.photos/800/450?random=16',
(SELECT id FROM categories WHERE name = '健康养生'),
true, 18.90, 'APPROVED', 8760, 1230, 760, 87,
'2025-08-20 14:10:00', '2025-08-20 14:00:00', NOW());

-- Entertainment Articles (Blogger 5: Entertainment)
INSERT INTO articles (user_id, title, content, summary, cover_image, category_id, is_paid, price, 
                      review_status, view_count, like_count, favorite_count, purchase_count, 
                      published_at, created_at, updated_at) VALUES

-- Article 17: AI Film
((SELECT id FROM users WHERE email = 'entertainment.blogger@blog.com'),
'First AI-Directed Film Wins Oscar: AI Artistic Creativity',
'<h2>Historic Moment</h2><p>2025 Oscars ceremony, first AI-directed film wins Best Picture...</p><h2>Creative Process</h2><p>How AI analyzes scripts, designs shots, directs actors...</p><h2>Artistic Debate</h2><p>Is AI creation true art...</p><h2>Future Outlook</h2><p>How AI will transform film industry...</p>',
'First AI-directed film wins Oscar. Exploring AI artistic creativity. Analyzing complete AI filmmaking process.',
'https://picsum.photos/800/450?random=17',
(SELECT id FROM categories WHERE name = '电子竞技'),
false, 0, 'APPROVED', 14560, 2010, 1340, 0,
'2025-03-15 20:30:00', '2025-03-15 20:00:00', NOW()),

-- Article 18: Virtual Idol
((SELECT id FROM users WHERE email = 'entertainment.blogger@blog.com'),
'Virtual Idol Concert: 100K Fans Party in Metaverse',
'<h2>Virtual Entertainment Era</h2><p>2025 hottest virtual idol holds metaverse concert, attracting 100K audience...</p><h2>Immersive Experience</h2><p>VR technology makes audience feel present...</p><h2>Interactive Features</h2><p>Audience can interact in real-time, even join on stage...</p><h2>Business Model</h2><p>Virtual concert profit model analysis...</p>',
'Experience virtual idol metaverse concert. 100K online party. Explore unlimited possibilities of virtual entertainment.',
'https://picsum.photos/800/450?random=18',
(SELECT id FROM categories WHERE name = '电子竞技'),
true, 12.90, 'APPROVED', 11230, 1560, 980, 95,
'2025-05-28 19:45:00', '2025-05-28 19:30:00', NOW()),

-- Article 19: Gaming Revolution
((SELECT id FROM users WHERE email = 'entertainment.blogger@blog.com'),
'BCI Gaming Experience: Control Characters with Thoughts',
'<h2>Gaming New Era</h2><p>2025 sees first BCI game officially released...</p><h2>Control Experience</h2><p>Control character movement and attacks with thoughts, ultimate immersion...</p><h2>Technical Principles</h2><p>How to convert brain signals into game commands...</p><h2>Game Review</h2><p>Detailed review of game content and experience...</p>',
'First BCI game deep dive. Control game characters with thoughts. Revolutionary breakthrough in gaming industry.',
'https://picsum.photos/800/450?random=19',
(SELECT id FROM categories WHERE name = '电子竞技'),
false, 0, 'APPROVED', 17890, 2450, 1670, 0,
'2025-07-08 21:20:00', '2025-07-08 21:00:00', NOW()),

-- Article 20: AI Music
((SELECT id FROM users WHERE email = 'entertainment.blogger@blog.com'),
'Rise of AI Composers: Can Machine Music Touch Hearts',
'<h2>AI Music Revolution</h2><p>2025 sees AI composer songs top music charts...</p><h2>Creative Ability</h2><p>How AI learns music theory, creates beautiful melodies...</p><h2>Emotional Expression</h2><p>Does AI music have real emotions...</p><h2>Musicians Future</h2><p>Will AI replace human musicians...</p>',
'Discussing rise of AI composers. Can machine music touch hearts. Where is music industry heading.',
'https://picsum.photos/800/450?random=20',
(SELECT id FROM categories WHERE name = '电子竞技'),
true, 16.90, 'APPROVED', 9650, 1340, 820, 78,
'2025-09-18 18:50:00', '2025-09-18 18:30:00', NOW());

-- Link articles with tags
INSERT INTO article_tags (article_id, tag_id) VALUES
-- Article 1: GPT-5
((SELECT id FROM articles WHERE title LIKE '%GPT-5%'), (SELECT id FROM tags WHERE name = 'AI')),
((SELECT id FROM articles WHERE title LIKE '%GPT-5%'), (SELECT id FROM tags WHERE name = 'ChatGPT')),
((SELECT id FROM articles WHERE title LIKE '%GPT-5%'), (SELECT id FROM tags WHERE name = 'LLM')),

-- Article 2: Quantum Computing
((SELECT id FROM articles WHERE title LIKE '%Quantum%'), (SELECT id FROM tags WHERE name = 'Quantum')),
((SELECT id FROM articles WHERE title LIKE '%Quantum%'), (SELECT id FROM tags WHERE name = 'Cloud')),

-- Article 3: 6G
((SELECT id FROM articles WHERE title LIKE '%6G%'), (SELECT id FROM tags WHERE name = '5G')),
((SELECT id FROM articles WHERE title LIKE '%6G%'), (SELECT id FROM tags WHERE name = 'IoT')),

-- Article 4: BCI
((SELECT id FROM articles WHERE title LIKE '%Neuralink%'), (SELECT id FROM tags WHERE name = 'BCI')),
((SELECT id FROM articles WHERE title LIKE '%Neuralink%'), (SELECT id FROM tags WHERE name = 'AI')),

-- Article 5: Sustainable Living
((SELECT id FROM articles WHERE title LIKE '%Sustainable%'), (SELECT id FROM tags WHERE name = 'Lifestyle')),

-- Article 6: Smart Home
((SELECT id FROM articles WHERE title LIKE '%Smart Home%'), (SELECT id FROM tags WHERE name = 'IoT')),
((SELECT id FROM articles WHERE title LIKE '%Smart Home%'), (SELECT id FROM tags WHERE name = 'SmartHome')),

-- Article 7: Food
((SELECT id FROM articles WHERE title LIKE '%Plant-Based%'), (SELECT id FROM tags WHERE name = 'Food')),

-- Article 8: Space Tourism
((SELECT id FROM articles WHERE title LIKE '%Space%'), (SELECT id FROM tags WHERE name = 'Travel')),

-- Article 9: AI Education
((SELECT id FROM articles WHERE title LIKE '%AI Personalized%'), (SELECT id FROM tags WHERE name = 'AI')),
((SELECT id FROM articles WHERE title LIKE '%AI Personalized%'), (SELECT id FROM tags WHERE name = 'OnlineLearning')),

-- Article 10: Metaverse Education
((SELECT id FROM articles WHERE title LIKE '%Metaverse Classroom%'), (SELECT id FROM tags WHERE name = 'Metaverse')),
((SELECT id FROM articles WHERE title LIKE '%Metaverse Classroom%'), (SELECT id FROM tags WHERE name = 'OnlineLearning')),

-- Article 11: Lifelong Learning
((SELECT id FROM articles WHERE title LIKE '%Lifelong%'), (SELECT id FROM tags WHERE name = 'Career')),
((SELECT id FROM articles WHERE title LIKE '%Lifelong%'), (SELECT id FROM tags WHERE name = 'Skills')),

-- Article 12: Career
((SELECT id FROM articles WHERE title LIKE '%Emerging Careers%'), (SELECT id FROM tags WHERE name = 'Career')),
((SELECT id FROM articles WHERE title LIKE '%Emerging Careers%'), (SELECT id FROM tags WHERE name = 'Skills')),

-- Article 13: Gene Editing
((SELECT id FROM articles WHERE title LIKE '%Gene Editing%'), (SELECT id FROM tags WHERE name = 'Wellness')),

-- Article 14: Anti-Aging
((SELECT id FROM articles WHERE title LIKE '%Anti-Aging%'), (SELECT id FROM tags WHERE name = 'AntiAging')),
((SELECT id FROM articles WHERE title LIKE '%Anti-Aging%'), (SELECT id FROM tags WHERE name = 'Wellness')),

-- Article 15: Mental Health
((SELECT id FROM articles WHERE title LIKE '%AI Therapist%'), (SELECT id FROM tags WHERE name = 'MentalHealth')),
((SELECT id FROM articles WHERE title LIKE '%AI Therapist%'), (SELECT id FROM tags WHERE name = 'AI')),

-- Article 16: Fitness
((SELECT id FROM articles WHERE title LIKE '%Fitness Mirror%'), (SELECT id FROM tags WHERE name = 'Fitness')),

-- Article 17: AI Film
((SELECT id FROM articles WHERE title LIKE '%AI-Directed%'), (SELECT id FROM tags WHERE name = 'Movies')),
((SELECT id FROM articles WHERE title LIKE '%AI-Directed%'), (SELECT id FROM tags WHERE name = 'AI')),

-- Article 18: Virtual Idol
((SELECT id FROM articles WHERE title LIKE '%Virtual Idol%'), (SELECT id FROM tags WHERE name = 'VirtualIdol')),
((SELECT id FROM articles WHERE title LIKE '%Virtual Idol%'), (SELECT id FROM tags WHERE name = 'Music')),

-- Article 19: BCI Gaming
((SELECT id FROM articles WHERE title LIKE '%BCI Gaming%'), (SELECT id FROM tags WHERE name = 'Games')),
((SELECT id FROM articles WHERE title LIKE '%BCI Gaming%'), (SELECT id FROM tags WHERE name = 'BCI')),

-- Article 20: AI Music
((SELECT id FROM articles WHERE title LIKE '%AI Composers%'), (SELECT id FROM tags WHERE name = 'Music')),
((SELECT id FROM articles WHERE title LIKE '%AI Composers%'), (SELECT id FROM tags WHERE name = 'AI'));

-- ========================================
-- Task 50.3: Initialize Sensitive Words
-- ========================================

-- Import common sensitive words list
INSERT INTO sensitive_words (word, created_at, updated_at) VALUES
-- Profanity
('idiot', NOW(), NOW()),
('stupid', NOW(), NOW()),
('moron', NOW(), NOW()),
('trash', NOW(), NOW()),
('garbage', NOW(), NOW()),
('screw you', NOW(), NOW()),
('go die', NOW(), NOW()),
('damn', NOW(), NOW()),

-- Adult content
('porn', NOW(), NOW()),
('xxx', NOW(), NOW()),
('nude', NOW(), NOW()),
('sex', NOW(), NOW()),

-- Violence
('kill', NOW(), NOW()),
('murder', NOW(), NOW()),
('suicide', NOW(), NOW()),
('bomb', NOW(), NOW()),
('terror', NOW(), NOW()),

-- Gambling
('casino', NOW(), NOW()),
('betting', NOW(), NOW()),
('gamble', NOW(), NOW()),

-- Drugs
('drug', NOW(), NOW()),
('heroin', NOW(), NOW()),
('cocaine', NOW(), NOW()),
('marijuana', NOW(), NOW()),

-- Fraud
('scam', NOW(), NOW()),
('fraud', NOW(), NOW()),
('cheat money', NOW(), NOW()),
('pyramid scheme', NOW(), NOW()),

-- Other inappropriate content
('doxxing', NOW(), NOW()),
('privacy leak', NOW(), NOW());

-- ========================================
-- Data Validation and Statistics
-- ========================================

-- Statistics of initialized data
SELECT '========== System Initialization Complete ==========' AS message;
SELECT COUNT(*) AS 'Admin Count' FROM users WHERE role = 'ADMIN';
SELECT COUNT(*) AS 'Blogger Count' FROM users WHERE role = 'BLOGGER';
SELECT COUNT(*) AS 'Category Count' FROM categories;
SELECT COUNT(*) AS 'Tag Count' FROM tags;
SELECT COUNT(*) AS 'Article Count' FROM articles;
SELECT COUNT(*) AS 'Paid Article Count' FROM articles WHERE is_paid = true;
SELECT COUNT(*) AS 'Article Tag Count' FROM article_tags;
SELECT COUNT(*) AS 'Sensitive Word Count' FROM sensitive_words;
SELECT '========================================' AS message;

-- Display sample article list
SELECT 
    a.id,
    a.title,
    c.name AS category,
    u.nickname AS author,
    a.is_paid,
    a.price,
    a.view_count,
    a.published_at
FROM articles a
JOIN users u ON a.user_id = u.id
JOIN categories c ON a.category_id = c.id
ORDER BY a.published_at DESC;
