package com.pcadvisor.common;

import com.pcadvisor.common.enums.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一API返回结果
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    private String requestId; // 请求ID，用于追踪

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = success();
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message, T data) {
        Result<T> result = error(code, message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message);
    }

    public static <T> Result<T> error(String message) {
        return error(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 是否成功
     */
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        // 先判空，再比较值（int和Integer==时，Integer自动拆箱为int）
        return this.code != null && ErrorCode.SUCCESS.getCode() == this.code;
    }

    /**
     * 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
    /**
     * 创建构建器
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * 构建器模式
     */
    public static class Builder<T> {
        private final Result<T> result;

        private Builder() {
            this.result = new Result<>();
        }

        public Builder<T> code(Integer code) {
            result.setCode(code);
            return this;
        }

        public Builder<T> message(String message) {
            result.setMessage(message);
            return this;
        }

        public Builder<T> data(T data) {
            result.setData(data);
            return this;
        }

        public Builder<T> requestId(String requestId) {
            result.setRequestId(requestId);
            return this;
        }

        public Result<T> build() {
            if (result.getCode() == null) {
                result.setCode(ErrorCode.SUCCESS.getCode());
            }
            if (result.getMessage() == null) {
                result.setMessage(ErrorCode.SUCCESS.getMessage());
            }
            return result;
        }
    }
}