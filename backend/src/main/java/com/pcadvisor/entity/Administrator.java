// Administrator.java - 管理员实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("Administrator")
public class Administrator implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "userID")
    private String userId;

    private String adminDept;
    private AdminLevel adminLevel;
    private LocalDateTime createTime;

    public enum AdminLevel {
        SUPER, NORMAL
    }

    // 关联的User对象
    private User user;
}