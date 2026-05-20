package com.pcadvisor.common.exception;

import com.pcadvisor.common.Result;
import com.pcadvisor.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}, URI: {}", e.getErrorCode(), e.getDetailMessage(), request.getRequestURI());

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("errorCode", e.getErrorCode().getCode());
        errorData.put("message", e.getDetailMessage());
        if (e.getData() != null) {
            errorData.put("data", e.getData());
        }

        return Result.error(e.getErrorCode().getCode(), e.getDetailMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证异常: {}, URI: {}", e.getMessage(), request.getRequestURI());
        return Result.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleAuthorizationException(AuthorizationException e, HttpServletRequest request) {
        log.warn("授权异常: {}, URI: {}", e.getMessage(), request.getRequestURI());
        return Result.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }

    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("数据验证异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("message", e.getMessage());
        if (e.getErrors() != null) {
            errorData.put("errors", e.getErrors());
        }

        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), e.getMessage(), errorData);
    }

    /**
     * 处理Spring Validation参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                HttpServletRequest request) {
        log.warn("参数校验异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ?
                                fieldError.getDefaultMessage() : "参数错误"
                ));

        String message = "参数验证失败";
        if (!errors.isEmpty()) {
            message = errors.values().iterator().next();
        }

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("errors", errors);

        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), message, errorData);
    }

    /**
     * 处理BindException异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("绑定异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ?
                                fieldError.getDefaultMessage() : "参数错误"
                ));

        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), "参数绑定失败", errors);
    }

    /**
     * 处理ConstraintViolationException异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e,
                                                             HttpServletRequest request) {
        log.warn("约束违反异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        Map<String, String> errors = e.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        return Result.error(ErrorCode.VALIDATION_ERROR.getCode(), "参数验证失败", errors);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                    HttpServletRequest request) {
        log.warn("参数类型不匹配异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");

        return Result.error(ErrorCode.PARAM_TYPE_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        String message = String.format("缺少必要参数: %s", e.getParameterName());
        return Result.error(ErrorCode.MISSING_PARAM.getCode(), message);
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                                HttpServletRequest request) {
        log.warn("HTTP消息不可读异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        String message = "请求体格式错误，请检查JSON格式";
        return Result.error(ErrorCode.PARSE_ERROR.getCode(), message);
    }

    /**
     * 处理文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                               HttpServletRequest request) {
        log.warn("文件大小超限异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        return Result.error(ErrorCode.FILE_TOO_LARGE.getCode(), "上传文件大小超过限制");
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Object> handleResourceNotFoundException(ResourceNotFoundException e,
                                                          HttpServletRequest request) {
        log.warn("资源未找到异常: {}, URI: {}", e.getMessage(), request.getRequestURI());

        return Result.error(ErrorCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage());
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleDatabaseException(DatabaseException e, HttpServletRequest request) {
        log.error("数据库异常: {}, URI: {}", e.getMessage(), request.getRequestURI(), e.getCause());

        return Result.error(ErrorCode.DATABASE_ERROR.getCode(), "数据库操作失败");
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleGlobalException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}, URI: {}", e.getMessage(), request.getRequestURI(), e);

        // 生产环境返回通用错误信息，开发环境返回详细错误信息
        String message = "系统内部错误，请稍后再试";
        Object data = null;

        // 开发环境下返回详细错误信息
        if (isDevEnvironment()) {
            message = e.getMessage();
            data = getExceptionDetail(e);
        }

        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), message, data);
    }

    /**
     * 获取异常详细信息
     */
    private Map<String, Object> getExceptionDetail(Exception e) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("exception", e.getClass().getName());
        detail.put("message", e.getMessage());

        // 堆栈信息（仅前5行）
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            detail.put("stackTrace", stackTrace[0].toString());
        }

        if (e.getCause() != null) {
            detail.put("cause", e.getCause().toString());
        }

        return detail;
    }

    /**
     * 判断是否是开发环境
     */
    private boolean isDevEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        return "dev".equals(env) || "local".equals(env);
    }
}