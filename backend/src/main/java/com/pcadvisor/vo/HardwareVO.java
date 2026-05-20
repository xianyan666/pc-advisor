// HardwareVO.java - 硬件视图对象
package com.pcadvisor.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class HardwareVO {
    private Integer hardwareId;
    private String hardwareName;
    private String hardwareType;
    private String hardwareBrand;
    private String hardwareModel;
    private BigDecimal hardwarePrice;
    private String auditState;
    private LocalDateTime publishTime;

    // 硬件详情
    private Map<String, Object> details;

    // 扩展信息
    private Integer collectCount;
    private Integer evaluationCount;
    private Boolean isCollected;
    private Double averageRating;

    // 推荐指数
    private Integer recommendationScore;
    private String recommendationReason;
}