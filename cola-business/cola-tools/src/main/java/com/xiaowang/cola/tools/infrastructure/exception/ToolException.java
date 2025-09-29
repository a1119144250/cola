package com.xiaowang.cola.tools.infrastructure.exception;

import com.xiaowang.cola.base.exception.BizException;
import com.xiaowang.cola.base.exception.ErrorCode;

/**
 * 工具模块异常
 *
 * @author cola
 */
public class ToolException extends BizException {

    public ToolException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ToolException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public ToolException(Throwable cause, ErrorCode errorCode) {
        super(cause, errorCode);
    }

    public ToolException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }
}
