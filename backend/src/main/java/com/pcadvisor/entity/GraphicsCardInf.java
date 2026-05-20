// GraphicsCardInf.java - 显卡详情实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("GraphicsCard_Inf")
public class GraphicsCardInf implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "GCID", type = IdType.AUTO)
    private Integer gcId;

    private Integer hardwareId;
    private String coreModel;
    private String vramCap;
    private String vramType;
    private Integer powerConsump;
    private Integer gcLength;

    // 关联的硬件主信息
    @TableField(exist = false)
    private HardwareInf hardwareInf;
}