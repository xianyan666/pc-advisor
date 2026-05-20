// Evaluation.java - 硬件测评实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("Evaluation")
public class Evaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "EvaluationID", type = IdType.AUTO)
    private Integer evaluationId;

    private String userId;
    private Integer hardwareId;
    private String evaluationTitle;
    private String auditState;
    private LocalDateTime publishTime;
    private LocalDateTime auditTime;

    @TableField("PerfTestData")
    private String perfTestData;

    @TableField("UsageExperience")
    private String usageExperience;

    @TableField("ProsAndCons")
    private String prosAndCons;

    // 关联信息
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private HardwareInf hardwareInf;

    @TableField(exist = false)
    private List<Comment> comments;

    @TableField(exist = false)
    private List<MediaInf> images;

    // 统计字段
    @TableField(exist = false)
    private Integer commentCount;

    @TableField(exist = false)
    private Boolean isCollected;
}