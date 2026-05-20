package com.pcadvisor.common.exception;

import com.pcadvisor.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异常工具类
 */
@Slf4j
public class ExceptionUtils {

    private static final Map<Class<? extends Throwable>, ErrorCode> EXCEPTION_ERROR_CODE_MAP =
            new ConcurrentHashMap<>();

    static {
        // 注册异常与错误码的映射关系
        EXCEPTION_ERROR_CODE_MAP.put(BusinessException.class, ErrorCode.BUSINESS_ERROR);
        EXCEPTION_ERROR_CODE_MAP.put(AuthenticationException.class, ErrorCode.UNAUTHORIZED);
        EXCEPTION_ERROR_CODE_MAP.put(AuthorizationException.class, ErrorCode.FORBIDDEN);
        EXCEPTION_ERROR_CODE_MAP.put(ValidationException.class, ErrorCode.VALIDATION_ERROR);
        EXCEPTION_ERROR_CODE_MAP.put(ResourceNotFoundException.class, ErrorCode.RESOURCE_NOT_FOUND);
        EXCEPTION_ERROR_CODE_MAP.put(DatabaseException.class, ErrorCode.DATABASE_ERROR);
        EXCEPTION_ERROR_CODE_MAP.put(ServiceException.class, ErrorCode.SERVICE_ERROR);
        EXCEPTION_ERROR_CODE_MAP.put(RateLimitException.class, ErrorCode.RATE_LIMIT);
        EXCEPTION_ERROR_CODE_MAP.put(FileUploadException.class, ErrorCode.FILE_UPLOAD_ERROR);
    }

    /**
     * 根据异常类型获取错误码
     */
    public static ErrorCode getErrorCode(Throwable e) {
        if (e == null) {
            return ErrorCode.SYSTEM_ERROR;
        }

        Class<? extends Throwable> clazz = e.getClass();
        while (clazz != null) {
            ErrorCode errorCode = EXCEPTION_ERROR_CODE_MAP.get(clazz);
            if (errorCode != null) {
                return errorCode;
            }
            clazz = (Class<? extends Throwable>) clazz.getSuperclass();
        }

        return ErrorCode.SYSTEM_ERROR;
    }

    /**
     * 创建业务异常
     */
    public static BusinessException createBusinessException(String message) {
        return new BusinessException(message);
    }

    /**
     * 创建业务异常
     */
    public static BusinessException createBusinessException(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message);
    }

    /**
     * 创建验证异常
     */
    public static ValidationException createValidationException(String field, String error) {
        return new ValidationException(field, error);
    }

    /**
     * 创建资源未找到异常
     */
    public static ResourceNotFoundException createResourceNotFoundException(String resourceName, Object identifier) {
        return new ResourceNotFoundException(resourceName, identifier);
    }

    /**
     * 创建数据库异常
     */
    public static DatabaseException createDatabaseException(String operation, Throwable cause) {
        String message = String.format("数据库操作失败: %s", operation);
        log.error(message, cause);
        return new DatabaseException(message, cause);
    }

    /**
     * 包装异常为业务异常
     */
    public static BusinessException wrapAsBusinessException(Throwable e) {
        if (e instanceof BusinessException) {
            return (BusinessException) e;
        }

        String message = e.getMessage();
        if (!StringUtils.hasText(message)) {
            message = "系统异常";
        }

        return new BusinessException(getErrorCode(e), message);
    }

    /**
     * 记录异常日志
     */
    public static void logException(String context, Throwable e) {
        if (e instanceof BusinessException) {
            // 业务异常只记录警告日志
            BusinessException be = (BusinessException) e;
            log.warn("业务异常[{}]: {} - {}", context, be.getErrorCode(), be.getDetailMessage());
        } else {
            // 其他异常记录错误日志
            log.error("系统异常[{}]: {}", context, e.getMessage(), e);
        }
    }

    /**
     * 判断异常是否需要告警
     */
    public static boolean needAlert(Throwable e) {
        if (e instanceof BusinessException) {
            BusinessException be = (BusinessException) e;
            return be.getErrorCode().isServerError();
        }
        return true;
    }

    /**
     * 获取异常的根原因
     */
    public static Throwable getRootCause(Throwable e) {
        if (e == null) {
            return null;
        }

        Throwable rootCause = e;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * 获取异常堆栈信息字符串
     */
    public static String getStackTraceAsString(Throwable e) {
        if (e == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            sb.append("\tat ").append(element).append("\n");
        }

        Throwable cause = e.getCause();
        if (cause != null) {
            sb.append("Caused by: ").append(getStackTraceAsString(cause));
        }

        return sb.toString();
    }
}