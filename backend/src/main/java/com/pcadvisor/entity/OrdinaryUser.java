// OrdinaryUser.java - 普通用户实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("OrdinaryUser")
public class OrdinaryUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "userID")
    private String userId;

    @TableField("userName")
    private String userName;

    @TableField("userPhone")
    private String userPhone;

    @TableField("userMailbox")
    private String userMailbox;

    @TableField("RegisterTime")
    private LocalDateTime registerTime;

    @TableField(exist = false)
    private User user;
}