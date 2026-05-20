-- PC硬件推荐系统 数据库初始化脚本
-- 在 pcadvisor 数据库中执行

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 一、清理旧表
-- =============================================
DROP TABLE IF EXISTS `Media_Inf`;
DROP TABLE IF EXISTS `Evaluation`;
DROP TABLE IF EXISTS `Collect`;
DROP TABLE IF EXISTS `Comment`;
DROP TABLE IF EXISTS `CPU_Inf`;
DROP TABLE IF EXISTS `GraphicsCard_Inf`;
DROP TABLE IF EXISTS `Motherboard_Inf`;
DROP TABLE IF EXISTS `Hardware_Inf`;
DROP TABLE IF EXISTS `Administrator`;
DROP TABLE IF EXISTS `OrdinaryUser`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `user`;

-- =============================================
-- 二、用户相关表
-- =============================================

-- 2.1 系统用户表（登录认证用）
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '业务用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(MD5加密)',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'ORDINARY' COMMENT '角色: ADMIN/ORDINARY',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2.2 普通用户表
CREATE TABLE IF NOT EXISTS `OrdinaryUser` (
    `userID` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `userName` VARCHAR(64) NOT NULL COMMENT '用户名',
    `userPhone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `userMailbox` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `RegisterTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='普通用户表';

-- 2.3 管理员表
CREATE TABLE IF NOT EXISTS `Administrator` (
    `userID` VARCHAR(32) NOT NULL COMMENT '管理员ID',
    `adminDept` VARCHAR(64) DEFAULT NULL COMMENT '部门',
    `adminLevel` VARCHAR(20) DEFAULT NULL COMMENT '管理员级别: SUPER/NORMAL',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`userID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =============================================
-- 三、硬件相关表
-- =============================================

-- 3.1 硬件主表
CREATE TABLE IF NOT EXISTS `Hardware_Inf` (
    `HardwareID` INT NOT NULL AUTO_INCREMENT COMMENT '硬件ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '发布者ID',
    `hardware_name` VARCHAR(256) NOT NULL COMMENT '硬件名称',
    `hardware_type` VARCHAR(32) NOT NULL COMMENT '硬件类型: CPU/MOTHERBOARD/GRAPHICS_CARD/MEMORY/STORAGE/POWER_SUPPLY/CASE/COOLER',
    `hardware_brand` VARCHAR(64) DEFAULT NULL COMMENT '品牌',
    `hardware_model` VARCHAR(64) DEFAULT NULL COMMENT '型号',
    `hardware_price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    `audit_state` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态',
    `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`HardwareID`),
    KEY `idx_hardware_type` (`hardware_type`),
    KEY `idx_hardware_brand` (`hardware_brand`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='硬件信息主表';

-- 3.2 CPU详情表
CREATE TABLE IF NOT EXISTS `CPU_Inf` (
    `CPUID` INT NOT NULL AUTO_INCREMENT COMMENT 'CPU详情ID',
    `hardware_id` INT NOT NULL COMMENT '关联硬件ID',
    `core_count` INT DEFAULT NULL COMMENT '核心数',
    `thread_count` INT DEFAULT NULL COMMENT '线程数',
    `base_freq` DECIMAL(5,2) DEFAULT NULL COMMENT '基础频率(GHz)',
    `interface_type` VARCHAR(32) DEFAULT NULL COMMENT '接口类型',
    `tdp_power` INT DEFAULT NULL COMMENT 'TDP功耗(W)',
    `support_mem_type` VARCHAR(32) DEFAULT NULL COMMENT '支持内存类型',
    PRIMARY KEY (`CPUID`),
    KEY `idx_cpu_hardware_id` (`hardware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CPU详情表';

-- 3.3 显卡详情表
CREATE TABLE IF NOT EXISTS `GraphicsCard_Inf` (
    `GCID` INT NOT NULL AUTO_INCREMENT COMMENT '显卡详情ID',
    `hardware_id` INT NOT NULL COMMENT '关联硬件ID',
    `core_model` VARCHAR(64) DEFAULT NULL COMMENT '核心型号',
    `vram_cap` VARCHAR(16) DEFAULT NULL COMMENT '显存容量',
    `vram_type` VARCHAR(16) DEFAULT NULL COMMENT '显存类型',
    `power_consump` INT DEFAULT NULL COMMENT '功耗(W)',
    `gc_length` INT DEFAULT NULL COMMENT '显卡长度(mm)',
    PRIMARY KEY (`GCID`),
    KEY `idx_gc_hardware_id` (`hardware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='显卡详情表';

-- 3.4 主板详情表
CREATE TABLE IF NOT EXISTS `Motherboard_Inf` (
    `MBDID` INT NOT NULL AUTO_INCREMENT COMMENT '主板详情ID',
    `hardware_id` INT NOT NULL COMMENT '关联硬件ID',
    `cpu_interface` VARCHAR(32) DEFAULT NULL COMMENT 'CPU接口',
    `mb_form` VARCHAR(16) DEFAULT NULL COMMENT '板型',
    `mem_slot_count` INT DEFAULT NULL COMMENT '内存插槽数',
    `support_mem_type` VARCHAR(32) DEFAULT NULL COMMENT '支持内存类型',
    `m2_slot_count` INT DEFAULT NULL COMMENT 'M.2插槽数',
    PRIMARY KEY (`MBDID`),
    KEY `idx_mb_hardware_id` (`hardware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主板详情表';

-- =============================================
-- 四、评测 / 收藏 / 评论 / 媒体表
-- =============================================

-- 4.1 评测表
CREATE TABLE IF NOT EXISTS `Evaluation` (
    `EvaluationID` INT NOT NULL AUTO_INCREMENT COMMENT '评测ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '发布者ID',
    `hardware_id` INT DEFAULT NULL COMMENT '关联硬件ID',
    `evaluation_title` VARCHAR(256) DEFAULT NULL COMMENT '评测标题',
    `audit_state` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态',
    `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `PerfTestData` TEXT COMMENT '性能测试数据(JSON)',
    `UsageExperience` TEXT COMMENT '使用体验',
    `ProsAndCons` TEXT COMMENT '优缺点',
    PRIMARY KEY (`EvaluationID`),
    KEY `idx_eval_hardware_id` (`hardware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评测表';

-- 4.2 评论表
CREATE TABLE IF NOT EXISTS `Comment` (
    `CommentID` VARCHAR(32) NOT NULL COMMENT '评论ID',
    `evaluation_id` INT DEFAULT NULL COMMENT '关联评测ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '评论者ID',
    `content` TEXT COMMENT '评论内容',
    `audit_state` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态',
    `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`CommentID`),
    KEY `idx_cmt_evaluation_id` (`evaluation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 4.3 收藏表
CREATE TABLE IF NOT EXISTS `Collect` (
    `CollectID` VARCHAR(32) NOT NULL COMMENT '收藏ID',
    `hardware_id` INT DEFAULT NULL COMMENT '关联硬件ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '收藏者ID',
    `collect_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`CollectID`),
    KEY `idx_collect_hardware_id` (`hardware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 4.4 媒体资源表
CREATE TABLE IF NOT EXISTS `Media_Inf` (
    `MediaID` INT NOT NULL AUTO_INCREMENT COMMENT '媒体资源ID',
    `evaluation_id` INT DEFAULT NULL COMMENT '关联评测ID',
    `comment_id` VARCHAR(32) DEFAULT NULL COMMENT '关联评论ID',
    `hardware_id` INT DEFAULT NULL COMMENT '关联硬件ID',
    `media_url` VARCHAR(512) NOT NULL COMMENT '媒体资源路径',
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`MediaID`),
    KEY `idx_media_hardware_id` (`hardware_id`),
    KEY `idx_media_evaluation_id` (`evaluation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体资源表';

-- =============================================
-- 五、用户测试数据
-- =============================================

INSERT INTO `sys_user` (`user_id`, `username`, `password`, `email`, `role`) VALUES
('U00000001', 'admin001', '0192023a7bbd73250516f069df18b500', 'admin@pcadvisor.com', 'ADMIN');

INSERT INTO `Administrator` (`userID`, `adminDept`, `adminLevel`, `createTime`) VALUES
('U00000001', '技术部', 'SUPER', NOW());

INSERT INTO `sys_user` (`user_id`, `username`, `password`, `email`, `role`) VALUES
('U00000002', 'testuser', 'cc03e747a6afbbcbf8be7668acfebee5', 'test@example.com', 'ORDINARY');

INSERT INTO `OrdinaryUser` (`userID`, `userName`, `userPhone`, `userMailbox`) VALUES
('U00000002', 'testuser', '13800138000', 'test@example.com');

-- =============================================
-- 六、硬件种子数据（名称与首页展示一致）
-- =============================================

-- === CPU ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('Intel 酷睿 i9-14900K', 'CPU', 'Intel', 'BX8071514900K', 4299.00, 'approved', NOW());
SET @cpu1 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu1, 24, 32, 3.2, 'LGA 1700', 125, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Ryzen 9 7950X', 'CPU', 'AMD', '100-100000514WOF', 3999.00, 'approved', NOW());
SET @cpu2 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu2, 16, 32, 4.5, 'AM5', 170, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Ryzen 7 7800X3D', 'CPU', 'AMD', '100-100000910WOF', 2499.00, 'approved', NOW());
SET @cpu3 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu3, 8, 16, 4.2, 'AM5', 120, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('Intel 酷睿 i7-14700K', 'CPU', 'Intel', 'BX8071514700K', 3299.00, 'approved', NOW());
SET @cpu4 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu4, 20, 28, 3.4, 'LGA 1700', 125, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Ryzen 9 9950X3D', 'CPU', 'AMD', '100-100001277WOF', 5999.00, 'approved', NOW());
SET @cpu5 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu5, 16, 32, 4.3, 'AM5', 170, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('Intel 酷睿 i9-14900KS', 'CPU', 'Intel', 'BX8071514900KS', 4599.00, 'approved', NOW());
SET @cpu6 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu6, 24, 32, 3.2, 'LGA 1700', 150, 'DDR5');

-- === 显卡 ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('NVIDIA GeForce RTX 5090 24GB', 'GRAPHICS_CARD', 'NVIDIA', 'RTX 5090 Founders Edition', 9999.00, 'approved', NOW());
SET @gpu1 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu1, 'GB202', '24GB', 'GDDR7', 575, 304);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('华硕 ROG Strix RTX 4070 Ti', 'GRAPHICS_CARD', '华硕', 'ROG-STRIX-RTX4070TI-O12G', 6299.00, 'approved', NOW());
SET @gpu2 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu2, 'AD104', '12GB', 'GDDR6X', 285, 336);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('七彩虹 RTX 5080', 'GRAPHICS_CARD', '七彩虹', 'iGame RTX 5080 Ultra', 7999.00, 'approved', NOW());
SET @gpu3 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu3, 'GB203', '16GB', 'GDDR7', 360, 330);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('NVIDIA GeForce RTX 5080', 'GRAPHICS_CARD', 'NVIDIA', 'RTX 5080 Founders Edition', 7999.00, 'approved', NOW());
SET @gpu4 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu4, 'GB203', '16GB', 'GDDR7', 360, 304);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Radeon RX 9070 XT', 'GRAPHICS_CARD', 'AMD', 'Radeon RX 9070 XT', 5499.00, 'approved', NOW());
SET @gpu5 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu5, 'Navi 48', '16GB', 'GDDR6', 304, 287);

-- === 主板 ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('华硕 ROG Maximus Z790', 'MOTHERBOARD', '华硕', 'ROG MAXIMUS Z790 HERO', 3299.00, 'approved', NOW());
SET @mb1 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb1, 'LGA 1700', 'ATX', 4, 'DDR5', 5);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('微星 MPG Z790 EDGE WIFI', 'MOTHERBOARD', '微星', 'MPG Z790 EDGE WIFI', 2199.00, 'approved', NOW());
SET @mb2 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb2, 'LGA 1700', 'ATX', 4, 'DDR5', 5);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('华硕 ROG Maximus Z890', 'MOTHERBOARD', '华硕', 'ROG MAXIMUS Z890 HERO', 3299.00, 'approved', NOW());
SET @mb3 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb3, 'LGA 1851', 'ATX', 4, 'DDR5', 6);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('微星 MPG Z890 Carbon WiFi', 'MOTHERBOARD', '微星', 'MPG Z890 CARBON WIFI', 2999.00, 'approved', NOW());
SET @mb4 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb4, 'LGA 1851', 'ATX', 4, 'DDR5', 6);

-- =============================================
-- 七、补充硬件（预算 → 中端全覆盖）
-- =============================================

-- === 入门/中端 CPU (Intel LGA1700) ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('Intel 酷睿 i5-12400F', 'CPU', 'Intel', 'BX8071512400F', 780.00, 'approved', NOW());
SET @cpu7 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu7, 6, 12, 2.5, 'LGA 1700', 65, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('Intel 酷睿 i5-13400F', 'CPU', 'Intel', 'BX8071513400F', 1200.00, 'approved', NOW());
SET @cpu8 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu8, 10, 16, 2.5, 'LGA 1700', 65, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('Intel 酷睿 i5-14600KF', 'CPU', 'Intel', 'BX8071514600KF', 1800.00, 'approved', NOW());
SET @cpu9 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu9, 14, 20, 3.5, 'LGA 1700', 125, 'DDR5');

-- === 入门/中端 CPU (AMD AM5) ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Ryzen 5 7500F', 'CPU', 'AMD', '100-100000597WOF', 980.00, 'approved', NOW());
SET @cpu10 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu10, 6, 12, 3.7, 'AM5', 65, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Ryzen 5 7600', 'CPU', 'AMD', '100-100001015WOF', 1300.00, 'approved', NOW());
SET @cpu11 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu11, 6, 12, 3.8, 'AM5', 65, 'DDR5');

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('AMD Ryzen 7 7700', 'CPU', 'AMD', '100-100000592WOF', 1800.00, 'approved', NOW());
SET @cpu12 = LAST_INSERT_ID();
INSERT INTO CPU_Inf (hardware_id, core_count, thread_count, base_freq, interface_type, tdp_power, support_mem_type)
VALUES (@cpu12, 8, 16, 3.8, 'AM5', 65, 'DDR5');

-- === 入门/中端 显卡 (NVIDIA RTX40系) ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('七彩虹 RTX 4060 8GB', 'GRAPHICS_CARD', '七彩虹', 'iGame RTX 4060 Ultra W', 2299.00, 'approved', NOW());
SET @gpu6 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu6, 'AD107', '8GB', 'GDDR6', 115, 250);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('华硕 RTX 4060 Ti 8GB', 'GRAPHICS_CARD', '华硕', 'DUAL-RTX4060TI-O8G', 3299.00, 'approved', NOW());
SET @gpu7 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu7, 'AD106', '8GB', 'GDDR6', 160, 270);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('微星 RTX 4070 SUPER 12GB', 'GRAPHICS_CARD', '微星', 'RTX 4070 SUPER VENTUS 2X', 4899.00, 'approved', NOW());
SET @gpu8 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu8, 'AD104', '12GB', 'GDDR6X', 220, 280);

-- === 入门/中端 显卡 (AMD Radeon) ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('蓝宝石 RX 7600 8GB', 'GRAPHICS_CARD', '蓝宝石', 'RX 7600 PULSE', 1899.00, 'approved', NOW());
SET @gpu9 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu9, 'Navi 33', '8GB', 'GDDR6', 165, 240);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('撼讯 RX 7800 XT 16GB', 'GRAPHICS_CARD', '撼讯', 'RX 7800 XT Hellhound', 3899.00, 'approved', NOW());
SET @gpu10 = LAST_INSERT_ID();
INSERT INTO GraphicsCard_Inf (hardware_id, core_model, vram_cap, vram_type, power_consump, gc_length)
VALUES (@gpu10, 'Navi 32', '16GB', 'GDDR6', 263, 320);

-- === 入门/中端 主板 (LGA1700) ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('华硕 PRIME H610M-A D4', 'MOTHERBOARD', '华硕', 'PRIME H610M-A D4', 550.00, 'approved', NOW());
SET @mb5 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb5, 'LGA 1700', 'M-ATX', 2, 'DDR4', 1);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('微星 PRO B760M-G', 'MOTHERBOARD', '微星', 'PRO B760M-G', 799.00, 'approved', NOW());
SET @mb6 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb6, 'LGA 1700', 'M-ATX', 2, 'DDR5', 2);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('华硕 TUF GAMING B760M-PLUS WIFI', 'MOTHERBOARD', '华硕', 'TUF GAMING B760M-PLUS WIFI', 1099.00, 'approved', NOW());
SET @mb7 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb7, 'LGA 1700', 'M-ATX', 4, 'DDR5', 2);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('微星 MAG Z790 TOMAHAWK WIFI', 'MOTHERBOARD', '微星', 'MAG Z790 TOMAHAWK WIFI', 1899.00, 'approved', NOW());
SET @mb8 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb8, 'LGA 1700', 'ATX', 4, 'DDR5', 4);

-- === 入门/中端 主板 (AMD AM5) ===
INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('技嘉 B650M GAMING WIFI', 'MOTHERBOARD', '技嘉', 'B650M GAMING WIFI', 899.00, 'approved', NOW());
SET @mb9 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb9, 'AM5', 'M-ATX', 2, 'DDR5', 1);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('技嘉 B650 AORUS ELITE AX', 'MOTHERBOARD', '技嘉', 'B650 AORUS ELITE AX', 1499.00, 'approved', NOW());
SET @mb10 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb10, 'AM5', 'ATX', 4, 'DDR5', 3);

INSERT INTO Hardware_Inf (hardware_name, hardware_type, hardware_brand, hardware_model, hardware_price, audit_state, publish_time)
VALUES ('微星 MAG X670E TOMAHAWK WIFI', 'MOTHERBOARD', '微星', 'MAG X670E TOMAHAWK WIFI', 2199.00, 'approved', NOW());
SET @mb11 = LAST_INSERT_ID();
INSERT INTO Motherboard_Inf (hardware_id, cpu_interface, mb_form, mem_slot_count, support_mem_type, m2_slot_count)
VALUES (@mb11, 'AM5', 'ATX', 4, 'DDR5', 4);

-- =============================================
-- 八、图片数据（关联硬件）
-- =============================================

-- NVIDIA 显卡
INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/GeForce RTX 5090.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%RTX 5090%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/rtx5090.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%RTX 5090%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/七彩虹 RTX 5080.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%七彩虹%RTX 5080%' LIMIT 1;

-- AMD 显卡
INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/Radeon RX 9070 XT.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%RX 9070 XT%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/华硕 ROG Strix RTX 4070 Ti.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%RTX 4070 Ti%' LIMIT 1;

-- AMD CPU
INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/AMD Ryzen 9 7950X.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Ryzen 9 7950X%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/AMD Ryzen 7 7800X3D.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Ryzen 7 7800X3D%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/Ryzen 9 9950X3D.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Ryzen 9 9950X3D%' LIMIT 1;

-- Intel CPU
INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/Intel 酷睿 i7-14700K.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%i7-14700K%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/Intel 酷睿 i9-14900K.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%i9-14900K%' AND h.hardware_name NOT LIKE '%KS%' LIMIT 1;

-- 主板
INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/华硕 ROG Maximus Z790.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Maximus Z790%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/ASUS ROG Maximus Z890 Hero.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Maximus Z890%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/微星 MPG Z790 EDGE WIFI.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Z790 EDGE%' LIMIT 1;

INSERT INTO Media_Inf (hardware_id, media_url, upload_time)
SELECT h.HardwareID, '/images/products/MSI MPG Z890 Carbon WiFi.webp', NOW()
FROM Hardware_Inf h WHERE h.hardware_name LIKE '%Z890 Carbon%' LIMIT 1;

-- 其余未关联的图片以 NULL hardware_id 插入
INSERT IGNORE INTO Media_Inf (hardware_id, media_url, upload_time) VALUES
(NULL, '/images/products/GeForce RTX 5070 Ti.webp', NOW()),
(NULL, '/images/products/GeForce RTX 5070.webp', NOW()),
(NULL, '/images/products/GeForce RTX 5060 Ti.webp', NOW()),
(NULL, '/images/products/GeForce RTX 5060.webp', NOW()),
(NULL, '/images/products/Radeon RX 9070.webp', NOW()),
(NULL, '/images/products/Radeon RX 9060 XT.webp', NOW()),
(NULL, '/images/products/Ryzen 5 9600X.webp', NOW()),
(NULL, '/images/products/Ryzen 7 9700X.webp', NOW()),
(NULL, '/images/products/Ryzen 7 9800X3D.webp', NOW()),
(NULL, '/images/products/Intel Core Ultra 7 265KF.webp', NOW()),
(NULL, '/images/products/Intel Core Ultra 9 285K.webp', NOW()),
(NULL, '/images/products/ASUS ROG Strix X870E-E Gaming WiFi.webp', NOW()),
(NULL, '/images/products/Gigabyte X870E Aorus Master.webp', NOW()),
(NULL, '/images/products/MSI MAG B860 Tomahawk WiFi.webp', NOW()),
(NULL, '/images/products/MSI MAG X870 Tomahawk WiFi.webp', NOW());


-- 测试用户收藏的硬件 (U00000002: testuser)
INSERT INTO `Collect` (`CollectID`, `hardware_id`, `user_id`, `collect_time`) VALUES
('C001', 1, 'U00000002', NOW()),
('C002', 7, 'U00000002', NOW()),
('C003', 12, 'U00000002', NOW());

-- 测试用户发布的评测 (U00000002: testuser)
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

SET FOREIGN_KEY_CHECKS = 1;
