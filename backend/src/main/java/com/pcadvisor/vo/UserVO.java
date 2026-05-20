// UserVO.java - 用户视图对象
package com.pcadvisor.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private String userId;
    private String userName;
    private String userPhone;
    private String userMailbox;
    private String userType;
    private LocalDateTime registerTime;
    private Integer collectCount;
    private Integer evaluationCount;
    private Integer commentCount;
}