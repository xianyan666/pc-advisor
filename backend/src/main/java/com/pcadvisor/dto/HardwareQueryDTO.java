// HardwareQueryDTO.java - 硬件查询条件
package com.pcadvisor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class HardwareQueryDTO {
    private String hardwareName;
    private List<String> hardwareTypes;
    private List<String> hardwareBrands;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String auditState;
    private String sortField = "publishTime";
    private String sortOrder = "desc";
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    // 特定硬件查询条件
    private String cpuInterface;
    private String supportMemType;
    private String mbForm;
    private Integer minCoreCount;
    private Integer maxCoreCount;
    private String coreModel;
    private Integer minPower;
    private Integer maxPower;
}