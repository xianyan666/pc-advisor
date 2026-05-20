package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常基类
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage;
    private final Object data;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
        this.data = null;
    }

    public BusinessException(ErrorCode errorCode, String detailMessage, Object data) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
        this.data = data;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_ERROR;
        this.detailMessage = message;
        this.data = null;
    }

    public BusinessException(String message, Object data) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_ERROR;
        this.detailMessage = message;
        this.data = data;
    }
}