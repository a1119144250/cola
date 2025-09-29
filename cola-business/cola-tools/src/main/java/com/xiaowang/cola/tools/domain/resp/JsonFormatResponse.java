package com.xiaowang.cola.tools.domain.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON 格式化响应
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonFormatResponse {

    /**
     * 格式化后的 JSON 字符串
     */
    private String formattedJson;

    /**
     * 原始 JSON 字符串大小（字节）
     */
    private Long originalSize;

    /**
     * 格式化后 JSON 字符串大小（字节）
     */
    private Long formattedSize;

    /**
     * 压缩比例（仅在 MINIFY 模式下有意义）
     */
    private Double compressionRatio;

    /**
     * JSON 是否有效
     */
    private Boolean isValid;

    /**
     * 错误信息（如果 JSON 无效）
     */
    private String errorMessage;

    /**
     * 格式化类型
     */
    private String formatType;

    /**
     * 处理时间（毫秒）
     */
    private Long processTime;
}
