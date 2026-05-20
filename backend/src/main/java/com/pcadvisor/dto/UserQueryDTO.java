package com.pcadvisor.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserQueryDTO {
    private String keyword;  // 关键词搜索（用户名、邮箱、手机号）
    private LocalDateTime startTime;  // 注册开始时间
    private LocalDateTime endTime;    // 注册结束时间
    private String userType;          // 用户类型
    private String sortField = "registerTime";  // 排序字段
    private String sortOrder = "desc";          // 排序方向
    private Integer pageNum = 1;      // 页码
    private Integer pageSize = 10;    // 每页大小
}