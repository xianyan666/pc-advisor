// Collect.java - 收藏实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("Collect")
public class Collect implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("CollectID")
    private String collectId;

    private Integer hardwareId;
    private String userId;
    private LocalDateTime collectTime;

    // 关联信息
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private HardwareInf hardwareInf;
}