package com.xiaowang.cola.tools.infrastructure.exception;

import com.xiaowang.cola.base.exception.ErrorCode;

/**
 * 工具模块错误码
 *
 * @author cola
 */
public enum ToolErrorCode implements ErrorCode {

    /**
     * 工具不存在
     */
    TOOL_NOT_EXIST("TOOL_NOT_EXIST", "工具不存在"),

    /**
     * 重复的工具名称
     */
    DUPLICATE_TOOL_NAME("DUPLICATE_TOOL_NAME", "工具名称已存在"),

    /**
     * 工具操作失败
     */
    TOOL_OPERATE_FAILED("TOOL_OPERATE_FAILED", "工具操作失败"),

    /**
     * 工具状态异常
     */
    TOOL_STATUS_ERROR("TOOL_STATUS_ERROR", "工具状态异常"),

    /**
     * 工具配置错误
     */
    TOOL_CONFIG_ERROR("TOOL_CONFIG_ERROR", "工具配置错误");

    private final String code;
    private final String message;

    ToolErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
