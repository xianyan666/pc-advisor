package com.pcadvisor.common.enums;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足，禁止访问"),
    RESOURCE_NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    RATE_LIMIT(429, "请求过于频繁"),

    // 服务器错误 5xx
    SYSTEM_ERROR(500, "系统内部错误"),
    SERVICE_ERROR(501, "服务内部错误"),
    DATABASE_ERROR(502, "数据库操作失败"),

    // 业务错误 1000-1999
    BUSINESS_ERROR(1000, "业务异常"),
    VALIDATION_ERROR(1001, "数据验证失败"),
    PARAM_TYPE_ERROR(1002, "参数类型错误"),
    MISSING_PARAM(1003, "缺少必要参数"),
    PARSE_ERROR(1004, "数据解析错误"),

    // 用户相关 2000-2099
    USER_NOT_FOUND(2000, "用户不存在"),
    USERNAME_EXISTS(2001, "用户名已存在"),
    EMAIL_EXISTS(2002, "邮箱已注册"),
    PHONE_EXISTS(2003, "手机号已注册"),
    PASSWORD_ERROR(2004, "密码错误"),
    ACCOUNT_DISABLED(2005, "账号已被禁用"),
    LOGIN_EXPIRED(2006, "登录已过期"),

    // 硬件相关 2100-2199
    HARDWARE_NOT_FOUND(2100, "硬件不存在"),
    HARDWARE_AUDIT_FAILED(2101, "硬件审核失败"),
    HARDWARE_EXISTS(2102, "硬件已存在"),

    // 测评相关 2200-2299
    EVALUATION_NOT_FOUND(2200, "测评不存在"),
    EVALUATION_AUDIT_FAILED(2201, "测评审核失败"),

    // 评论相关 2300-2399
    COMMENT_NOT_FOUND(2300, "评论不存在"),
    COMMENT_AUDIT_FAILED(2301, "评论审核失败"),

    // 收藏相关 2400-2499
    COLLECT_EXISTS(2400, "已收藏该硬件"),
    COLLECT_NOT_FOUND(2401, "收藏不存在"),

    // 文件相关 2500-2599
    FILE_UPLOAD_ERROR(2500, "文件上传失败"),
    FILE_NOT_FOUND(2501, "文件不存在"),
    FILE_TOO_LARGE(2502, "文件大小超过限制"),
    FILE_TYPE_NOT_SUPPORTED(2503, "文件类型不支持"),

    // 推荐相关 2600-2699
    RECOMMENDATION_ERROR(2600, "推荐系统错误"),
    INSUFFICIENT_DATA(2601, "数据不足，无法生成推荐"),

    // 论坛相关 2700-2799
    POST_NOT_FOUND(2700, "帖子不存在"),
    POST_AUDIT_FAILED(2701, "帖子审核失败"),

    // 管理员相关 2800-2899
    ADMIN_NOT_FOUND(2800, "管理员不存在"),
    ADMIN_PERMISSION_DENIED(2801, "管理员权限不足");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }

    /**
     * 判断是否是客户端错误
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    /**
     * 判断是否是服务器错误
     */
    public boolean isServerError() {
        return this.code >= 500;
    }
}