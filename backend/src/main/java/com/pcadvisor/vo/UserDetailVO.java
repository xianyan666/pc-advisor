package com.pcadvisor.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDetailVO {
    private String userId;
    private String userName;
    private String userPhone;
    private String userMailbox;
    private String userType;  // ADMIN, ORDINARY
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private Integer collectCount;      // 收藏数量
    private Integer evaluationCount;   // 测评数量
    private Integer commentCount;      // 评论数量
    private String avatar;             // 头像URL
}