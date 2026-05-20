package com.pcadvisor.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String userId;
    private String role; // 用户角色
}