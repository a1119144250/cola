package com.xiaowang.cola.tools.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 工具创建参数
 *
 * @author cola
 */
@Data
public class ToolCreateParam {

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String toolName;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 工具类型
     */
    @NotBlank(message = "工具类型不能为空")
    private String toolType;

    /**
     * 工具版本
     */
    private String version;

    /**
     * 创建者
     */
    @NotBlank(message = "创建者不能为空")
    private String creator;
}
