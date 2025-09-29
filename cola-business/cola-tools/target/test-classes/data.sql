-- 初始化工具数据
INSERT INTO tool (tool_name, description, tool_type, version, status, creator, modifier) VALUES
('字符串工具', '提供字符串处理相关功能', 'SYSTEM', '1.0.0', 1, 'system', 'system'),
('时间工具', '提供时间处理相关功能', 'SYSTEM', '1.0.0', 1, 'system', 'system'),
('JSON工具', '提供JSON处理相关功能', 'DEVELOPMENT', '1.0.0', 1, 'system', 'system'),
('加密工具', '提供加密解密相关功能', 'SYSTEM', '1.0.0', 1, 'system', 'system'),
('网络工具', '提供网络请求相关功能', 'DEVELOPMENT', '1.0.0', 1, 'system', 'system');
