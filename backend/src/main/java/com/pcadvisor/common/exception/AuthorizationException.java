package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 授权异常（权限不足）
 */
public class AuthorizationException extends BusinessException {

    public AuthorizationException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    public AuthorizationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}