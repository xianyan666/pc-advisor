package com.pcadvisor.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcadvisor.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 */
public class ResponseUtils {

    // 单例ObjectMapper（线程安全，建议复用）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 可选：配置ObjectMapper（如支持JDK8时间类型、空值处理等）
        // OBJECT_MAPPER.registerModule(new JavaTimeModule()); // 支持LocalDateTime等
        // OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略空值
    }

    /**
     * 创建成功响应
     */
    public static <T> ResponseEntity<Result<T>> success(T data) {
        return ResponseEntity.ok(Result.success(data));
    }

    public static <T> ResponseEntity<Result<T>> success(String message, T data) {
        return ResponseEntity.ok(Result.success(message, data));
    }

    /**
     * 创建错误响应
     */
    public static <T> ResponseEntity<Result<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Result.error(status.value(), message));
    }

    public static <T> ResponseEntity<Result<T>> error(HttpStatus status, Integer code, String message) {
        return ResponseEntity.status(status).body(Result.error(code, message));
    }

    /**
     * 写入JSON响应
     */
    public static void writeJsonResponse(HttpServletResponse response, int status, String json)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(json);
    }

    /**
     * 写入错误响应
     */
    public static void writeErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        Result<Object> result = Result.error(status, message);
        String json;
        try {
            // 使用单例ObjectMapper序列化
            json = OBJECT_MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            // 序列化失败时的兜底处理
            json = String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", status, "响应序列化失败");
            e.printStackTrace(); // 生产环境建议替换为日志记录
        }
        writeJsonResponse(response, status, json);
    }

    /**
     * 设置响应头
     */
    public static void setResponseHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}