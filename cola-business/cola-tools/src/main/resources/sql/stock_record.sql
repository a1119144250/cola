-- 库存流水记录表
CREATE TABLE IF NOT EXISTS `stock_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` varchar(64) NOT NULL COMMENT '流水记录ID（业务唯一标识）',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
  `operation_type` varchar(32) NOT NULL COMMENT '操作类型（DEDUCT-扣减, ADD-增加, FREEZE-冻结, UNFREEZE-解冻）',
  `amount` int(11) NOT NULL COMMENT '操作数量',
  `before_stock` int(11) DEFAULT NULL COMMENT '操作前库存',
  `after_stock` int(11) DEFAULT NULL COMMENT '操作后库存',
  `user_id` varchar(64) DEFAULT NULL COMMENT '用户ID',
  `order_id` varchar(64) DEFAULT NULL COMMENT '订单ID',
  `scene` varchar(64) NOT NULL COMMENT '业务场景',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '流水状态（PENDING-待处理, COMPLETED-已完成, CANCELLED-已取消, RECONCILED-已对账）',
  `ext_info` text COMMENT '扩展信息（JSON格式）',
  `reconcile_time` datetime DEFAULT NULL COMMENT '对账时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0-否，1-是）',
  `lock_version` int(11) NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_id` (`record_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_gmt_create` (`gmt_create`),
  KEY `idx_product_status` (`product_id`, `status`),
  KEY `idx_reconcile_time` (`reconcile_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水记录表';

-- 创建分区表（按月分区，提高查询性能）
-- ALTER TABLE stock_record PARTITION BY RANGE (TO_DAYS(gmt_create)) (
--   PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
--   PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
--   PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')),
--   PARTITION p202404 VALUES LESS THAN (TO_DAYS('2024-05-01')),
--   PARTITION p202405 VALUES LESS THAN (TO_DAYS('2024-06-01')),
--   PARTITION p202406 VALUES LESS THAN (TO_DAYS('2024-07-01')),
--   PARTITION p202407 VALUES LESS THAN (TO_DAYS('2024-08-01')),
--   PARTITION p202408 VALUES LESS THAN (TO_DAYS('2024-09-01')),
--   PARTITION p202409 VALUES LESS THAN (TO_DAYS('2024-10-01')),
--   PARTITION p202410 VALUES LESS THAN (TO_DAYS('2024-11-01')),
--   PARTITION p202411 VALUES LESS THAN (TO_DAYS('2024-12-01')),
--   PARTITION p202412 VALUES LESS THAN (TO_DAYS('2025-01-01')),
--   PARTITION pmax VALUES LESS THAN MAXVALUE
-- );
