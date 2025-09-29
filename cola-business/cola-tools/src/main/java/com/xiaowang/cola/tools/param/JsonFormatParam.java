package com.xiaowang.cola.tools.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * JSON 格式化参数
 *
 * @author cola
 */
@Data
public class JsonFormatParam {

    /**
     * 待格式化的 JSON 字符串
     */
    @NotBlank(message = "JSON 字符串不能为空")
    private String jsonStr;

    /**
     * 格式化类型（PRETTY, COMPACT, MINIFY）
     */
    private String formatType = "PRETTY";

    /**
     * 缩进大小（仅在 PRETTY 模式下有效）
     */
    private Integer indentSize = 2;

    /**
     * 是否验证 JSON 语法
     */
    private Boolean validate = true;

    /**
     * 是否排序键名
     */
    private Boolean sortKeys = false;
}
