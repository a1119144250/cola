-- 工具表
CREATE TABLE IF NOT EXISTS tool (
    id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tool_name varchar(100) NOT NULL,
    description varchar(500) DEFAULT NULL,
    tool_type varchar(50) NOT NULL,
    version varchar(20) DEFAULT NULL,
    status tinyint NOT NULL DEFAULT 1,
    config_info text DEFAULT NULL,
    gmt_create timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator varchar(50) DEFAULT NULL,
    modifier varchar(50) DEFAULT NULL
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS uk_tool_name ON tool(tool_name);

-- 创建普通索引
CREATE INDEX IF NOT EXISTS idx_tool_type ON tool(tool_type);
CREATE INDEX IF NOT EXISTS idx_status ON tool(status);
CREATE INDEX IF NOT EXISTS idx_gmt_create ON tool(gmt_create);
