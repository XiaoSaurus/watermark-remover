-- 用户表字段扩展 - 用户资料完善
-- 执行时间: 2026-04-01
-- 说明: 添加用户等级、积分、VIP、个人简介等字段

ALTER TABLE `user` 
  ADD COLUMN `bio` VARCHAR(200) COMMENT '个人简介' AFTER `nickname`,
  ADD COLUMN `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女' AFTER `bio`,
  ADD COLUMN `birthday` DATE COMMENT '生日' AFTER `gender`,
  ADD COLUMN `level` INT DEFAULT 1 COMMENT '用户等级 1-10' AFTER `birthday`,
  ADD COLUMN `points` INT DEFAULT 0 COMMENT '积分' AFTER `level`,
  ADD COLUMN `vip_expire_at` TIMESTAMP NULL COMMENT 'VIP到期时间' AFTER `points`,
  ADD COLUMN `login_count` INT DEFAULT 0 COMMENT '登录次数' AFTER `vip_expire_at`;

-- 创建索引以提高查询性能
CREATE INDEX `idx_level` ON `user`(`level`);
CREATE INDEX `idx_points` ON `user`(`points`);
CREATE INDEX `idx_vip_expire_at` ON `user`(`vip_expire_at`);

-- 创建用户积分日志表（可选，用于记录积分变更）
CREATE TABLE IF NOT EXISTS `user_points_log` (
  `id` BIGINT NOT NULL PRIMARY KEY COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `points` INT NOT NULL COMMENT '积分变更数量',
  `reason` VARCHAR(100) COMMENT '积分变更原因',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分日志表';

-- 创建用户等级定义表（参考表）
CREATE TABLE IF NOT EXISTS `user_level_config` (
  `level` INT PRIMARY KEY COMMENT '等级',
  `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `min_points` INT NOT NULL COMMENT '最低积分',
  `description` VARCHAR(200) COMMENT '等级描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户等级配置表';

-- 初始化等级配置
INSERT INTO `user_level_config` (`level`, `name`, `min_points`, `description`) VALUES
(1, '新手', 0, '欢迎加入'),
(2, '初级', 100, '小有成就'),
(3, '中级', 500, '渐入佳境'),
(4, '高级', 1000, '技艺精湛'),
(5, '专家', 5000, '出类拔萃'),
(6, '大师', 10000, '登峰造极'),
(7, '宗师', 20000, '超凡入圣'),
(8, '王者', 50000, '王者风范'),
(9, '传奇', 100000, '传奇人物'),
(10, '至尊', 200000, '至尊荣耀');
