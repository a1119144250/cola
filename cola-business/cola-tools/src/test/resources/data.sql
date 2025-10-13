-- 测试数据：库存流水记录
-- 清空表数据
DELETE FROM stock_record;

-- 插入测试数据
INSERT INTO stock_record (
    record_id, product_id, operation_type, amount, before_stock, after_stock,
    user_id, order_id, scene, status, ext_info, remark, deleted, lock_version, gmt_create, gmt_modified
) VALUES 
-- 商品1的流水记录
('test_record_001', 'PRODUCT_001', 'DEDUCT', 5, 100, 95, 'USER_001', 'ORDER_001', 'SECKILL', 'COMPLETED', '{"channel":"APP"}', '秒杀扣减', 0, 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
('test_record_002', 'PRODUCT_001', 'DEDUCT', 3, 95, 92, 'USER_002', 'ORDER_002', 'SECKILL', 'COMPLETED', '{"channel":"WEB"}', '秒杀扣减', 0, 0, '2024-01-01 10:01:00', '2024-01-01 10:01:00'),
('test_record_003', 'PRODUCT_001', 'DEDUCT', 2, 92, 90, 'USER_003', 'ORDER_003', 'NORMAL_ORDER', 'RECONCILED', '{"channel":"APP"}', '普通订单', 0, 0, '2024-01-01 10:02:00', '2024-01-01 10:02:00'),

-- 商品2的流水记录
('test_record_004', 'PRODUCT_002', 'DEDUCT', 10, 200, 190, 'USER_004', 'ORDER_004', 'SECKILL', 'COMPLETED', '{"channel":"APP"}', '秒杀扣减', 0, 0, '2024-01-01 11:00:00', '2024-01-01 11:00:00'),
('test_record_005', 'PRODUCT_002', 'ADD', 5, 190, 195, 'ADMIN_001', NULL, 'RESTOCK', 'COMPLETED', '{"reason":"补货"}', '库存补充', 0, 0, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),

-- 商品3的流水记录（已对账）
('test_record_006', 'PRODUCT_003', 'DEDUCT', 1, 50, 49, 'USER_005', 'ORDER_005', 'NORMAL_ORDER', 'RECONCILED', '{"channel":"WEB"}', '普通订单', 0, 0, '2024-01-01 09:00:00', '2024-01-01 09:00:00'),
('test_record_007', 'PRODUCT_003', 'DEDUCT', 2, 49, 47, 'USER_006', 'ORDER_006', 'NORMAL_ORDER', 'RECONCILED', '{"channel":"APP"}', '普通订单', 0, 0, '2024-01-01 09:30:00', '2024-01-01 09:30:00');