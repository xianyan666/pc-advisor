package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;
import lombok.Getter;

import java.util.Map;

/**
 * 数据验证异常
 */
@Getter
public class ValidationException extends BusinessException {
    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.errors = null;
    }

    public ValidationException(Map<String, String> errors) {
        super(ErrorCode.VALIDATION_ERROR, "数据验证失败");
        this.errors = errors;
    }

    public ValidationException(String field, String error) {
        super(ErrorCode.VALIDATION_ERROR, String.format("字段[%s]验证失败: %s", field, error));
        this.errors = Map.of(field, error);
    }
}