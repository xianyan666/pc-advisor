// RecommendationVO.java - 推荐结果视图对象
package com.pcadvisor.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class RecommendationVO {
    private BigDecimal totalPrice;
    private BigDecimal originalPrice;
    private BigDecimal discount;
    private String purpose;
    private String performanceLevel;
    private Integer compatibilityScore;  // 兼容性评分 0-100

    // 配置列表
    private HardwareVO cpu;
    private HardwareVO motherboard;
    private HardwareVO graphicsCard;
    private HardwareVO memory;
    private HardwareVO storage;
    private HardwareVO powerSupply;
    private HardwareVO state;
    private List<HardwareVO> peripherals;

    // 分析结果
    private String strengths;
    private String weaknesses;
    private List<String> compatibilityIssues;
    private Map<String, BigDecimal> priceDistribution;

    // 升级建议
    private List<String> upgradeSuggestions;
}