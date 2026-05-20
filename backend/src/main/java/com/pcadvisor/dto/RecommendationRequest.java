// RecommendationRequest.java - 推荐请求参数
package com.pcadvisor.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecommendationRequest {
    private BigDecimal budget;          // 预算
    private String purpose;             // 用途：gaming, design, office, development
    private String performanceLevel;    // 性能等级：entry, mid, high, enthusiast
    private Boolean includePeripherals; // 是否包含外设
    private String preferredBrand;      // 品牌偏好
    private Integer needRGB;            // RGB需求等级 0-10
    private String sizePreference;      // 尺寸偏好：mini, compact, standard, full
}