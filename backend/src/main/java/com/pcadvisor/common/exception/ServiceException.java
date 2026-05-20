package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 服务层异常
 */
public class ServiceException extends BusinessException {

    public ServiceException(String message) {
        super(ErrorCode.SERVICE_ERROR, message);
    }

    public ServiceException(String message, Object data) {
        super(ErrorCode.SERVICE_ERROR, message, data);
    }

    public ServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}