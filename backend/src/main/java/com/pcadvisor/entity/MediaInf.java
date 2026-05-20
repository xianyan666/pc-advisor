// MediaInf.java - 媒体资源实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("Media_Inf")
public class MediaInf implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "MediaID", type = IdType.AUTO)
    private Integer mediaId;

    private Integer evaluationId;
    private String commentId;
    private Integer hardwareId;
    private String mediaUrl;
    private LocalDateTime uploadTime;

    // 关联信息
    @TableField(exist = false)
    private Evaluation evaluation;

    @TableField(exist = false)
    private Comment comment;

    @TableField(exist = false)
    private HardwareInf hardwareInf;
}