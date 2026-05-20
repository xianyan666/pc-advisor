-- PC硬件推荐系统 — 收藏与评测种子数据
-- 可直接在现有数据库中执行，不会删除已有数据
-- 使用前请确认 testuser (U00000002) 已存在

-- 测试用户收藏的硬件
INSERT IGNORE INTO `Collect` (`CollectID`, `hardware_id`, `user_id`, `collect_time`) VALUES
('C001', 1, 'U00000002', NOW()),
('C002', 7, 'U00000002', NOW()),
('C003', 12, 'U00000002', NOW());

-- 测试用户发布的评测
INSERT INTO `Evaluation` (`user_id`, `hardware_id`, `evaluation_title`, `audit_state`, `publish_time`, `audit_time`, `PerfTestData`, `UsageExperience`, `ProsAndCons`)
VALUES
('U00000002', 1, 'Intel 酷睿 i9-14900K 深度评测：24核性能怪兽',
 'approved', NOW(), NOW(),
 '{"Cinebench R23":{"单核":2250,"多核":38000},"CPU-Z":{"单核":920,"多核":16800},"3DMark CPU Profile":{"最大线程":16500}}',
 '作为Intel第14代旗舰处理器，i9-14900K在日常使用和游戏中的表现都非常出色。无论是多任务处理还是高强度游戏，它都能轻松应对。功耗控制比上一代有所提升，建议搭配360水冷使用。',
 '优点：单核性能强劲，游戏帧率高，多核渲染能力强\n缺点：功耗较高，满载温度需要好散热器压制'),
('U00000002', 7, 'RTX 5090 上手体验：次世代显卡的标杆',
 'approved', NOW(), NOW(),
 '{"3DMark Time Spy Extreme":{"图形分":18500},"3DMark Port Royal":{"总分":22000},"游戏测试":{"4K最高画质":120,"8K中画质":60}}',
 'RTX 5090毫无疑问是目前最强的消费级显卡。在4K分辨率下玩所有主流3A大作都毫无压力，光线追踪性能相比上代提升明显。DLSS 4的画质和帧率表现令人印象深刻。',
 '优点：性能无敌，DLSS 4表现出色，24GB大显存\n缺点：价格高昂，功耗高，体积大需要大机箱');
