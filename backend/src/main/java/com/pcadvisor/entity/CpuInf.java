// CpuInf.java - CPU详情实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("CPU_Inf")
public class CpuInf implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "CPUID", type = IdType.AUTO)
    private Integer cpuId;

    private Integer hardwareId;
    private Integer coreCount;
    private Integer threadCount;
    private BigDecimal baseFreq;
    private String interfaceType;
    private Integer tdpPower;
    private String supportMemType;

    // 关联的硬件主信息
    @TableField(exist = false)
    private HardwareInf hardwareInf;
}