package com.xiaowang.cola.tools.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 工具实体
 *
 * @author cola
 */
@Data
@Accessors(chain = true)
@TableName("tool")
public class Tool {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工具名称
     */
    @TableField("tool_name")
    private String toolName;

    /**
     * 工具描述
     */
    @TableField("description")
    private String description;

    /**
     * 工具类型
     */
    @TableField("tool_type")
    private String toolType;

    /**
     * 工具版本
     */
    @TableField("version")
    private String version;

    /**
     * 工具状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 配置信息
     */
    @TableField("config_info")
    private String configInfo;

    /**
     * 创建时间
     */
    @TableField("gmt_create")
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    @TableField("gmt_modified")
    private LocalDateTime gmtModified;

    /**
     * 创建者
     */
    @TableField("creator")
    private String creator;

    /**
     * 修改者
     */
    @TableField("modifier")
    private String modifier;

    public Tool() {
        this.gmtCreate = LocalDateTime.now();
        this.gmtModified = LocalDateTime.now();
        this.status = 1; // 默认启用
    }

    /**
     * 初始化工具
     */
    public void init(String toolName, String description, String toolType, String version, String creator) {
        this.toolName = toolName;
        this.description = description;
        this.toolType = toolType;
        this.version = version;
        this.creator = creator;
        this.modifier = creator;
        this.status = 1;
        this.gmtCreate = LocalDateTime.now();
        this.gmtModified = LocalDateTime.now();
    }

    /**
     * 更新工具信息
     */
    public void updateInfo(String description, String configInfo, String modifier) {
        this.description = description;
        this.configInfo = configInfo;
        this.modifier = modifier;
        this.gmtModified = LocalDateTime.now();
    }

    /**
     * 启用工具
     */
    public void enable(String modifier) {
        this.status = 1;
        this.modifier = modifier;
        this.gmtModified = LocalDateTime.now();
    }

    /**
     * 禁用工具
     */
    public void disable(String modifier) {
        this.status = 0;
        this.modifier = modifier;
        this.gmtModified = LocalDateTime.now();
    }
}
