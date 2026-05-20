package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;

/**
 * 请求频率限制异常
 */
public class RateLimitException extends BusinessException {

    public RateLimitException(String message) {
        super(ErrorCode.RATE_LIMIT, message);
    }

    public RateLimitException(long waitTime) {
        super(ErrorCode.RATE_LIMIT,
                String.format("请求过于频繁，请%d秒后重试", waitTime));
    }
}