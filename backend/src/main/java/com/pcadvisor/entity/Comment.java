// Comment.java - 评论实体
package com.pcadvisor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("Comment")
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("CommentID")
    private String commentId;

    private Integer evaluationId;
    private String userId;
    private String content;
    private String auditState;
    private LocalDateTime publishTime;
    private LocalDateTime auditTime;

    // 关联信息
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Evaluation evaluation;

    @TableField(exist = false)
    private List<MediaInf> images;
}