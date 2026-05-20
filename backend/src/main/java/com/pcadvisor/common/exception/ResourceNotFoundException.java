package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s[%s]不存在", resourceName, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}