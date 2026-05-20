// AuditState.java - 审核状态枚举
package com.pcadvisor.common.enums;

public enum AuditState {
    PENDING("pending", "待审核"),
    APPROVED("approved", "审核通过"),
    REJECTED("rejected", "审核未通过");

    private final String code;
    private final String description;

    AuditState(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AuditState fromCode(String code) {
        for (AuditState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        return null;
    }
}