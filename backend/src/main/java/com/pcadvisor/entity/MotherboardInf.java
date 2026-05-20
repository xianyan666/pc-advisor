// MotherboardInf.java - 主板详情实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("Motherboard_Inf")
public class MotherboardInf implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "MBDID", type = IdType.AUTO)
    private Integer mbdId;

    private Integer hardwareId;
    private String cpuInterface;
    private String mbForm;
    private Integer memSlotCount;
    private String supportMemType;
    private Integer m2SlotCount;

    // 关联的硬件主信息
    @TableField(exist = false)
    private HardwareInf hardwareInf;
}