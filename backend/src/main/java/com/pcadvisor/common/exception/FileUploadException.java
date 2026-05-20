package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 文件上传异常
 */
public class FileUploadException extends BusinessException {

    public FileUploadException(String message) {
        super(ErrorCode.FILE_UPLOAD_ERROR, message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(ErrorCode.FILE_UPLOAD_ERROR, message);
        this.initCause(cause);
    }
}