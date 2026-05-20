package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 认证异常（登录、权限相关）
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AuthenticationException(String message, Object data) {
        super(ErrorCode.UNAUTHORIZED, message, data);
    }
}