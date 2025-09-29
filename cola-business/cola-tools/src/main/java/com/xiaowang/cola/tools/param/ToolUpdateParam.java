package com.xiaowang.cola.tools.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工具更新参数
 *
 * @author cola
 */
@Data
public class ToolUpdateParam {

    /**
     * 工具ID
     */
    @NotNull(message = "工具ID不能为空")
    private Long toolId;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 配置信息
     */
    private String configInfo;

    /**
     * 修改者
     */
    private String modifier;
}
