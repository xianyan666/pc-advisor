// HardwareInf.java - 硬件信息主表实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("Hardware_Inf")
public class HardwareInf implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "HardwareID", type = IdType.AUTO)
    private Integer hardwareId;

    private String userId;
    private String hardwareName;
    private String hardwareType;
    private String hardwareBrand;
    private String hardwareModel;
    private BigDecimal hardwarePrice;
    private String auditState;
    private LocalDateTime publishTime;
    private LocalDateTime auditTime;
    private LocalDateTime updateTime;

    // 扩展字段 - 用于前端展示
    @TableField(exist = false)
    private String auditStateName;

    @TableField(exist = false)
    private String hardwareTypeName;

    // 子类详细信息
    @TableField(exist = false)
    private CpuInf cpuInf;

    @TableField(exist = false)
    private MotherboardInf motherboardInf;

    @TableField(exist = false)
    private GraphicsCardInf graphicsCardInf;
}