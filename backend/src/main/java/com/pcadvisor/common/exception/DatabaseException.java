package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 数据库操作异常
 */
public class DatabaseException extends BusinessException {

    public DatabaseException(String message) {
        super(ErrorCode.DATABASE_ERROR, message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, message);
        this.initCause(cause);
    }
}