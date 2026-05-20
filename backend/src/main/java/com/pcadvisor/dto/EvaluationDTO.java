// EvaluationDTO.java - 测评数据传输对象
package com.pcadvisor.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class EvaluationDTO {
    @NotNull(message = "硬件ID不能为空")
    private Integer hardwareId;

    @NotBlank(message = "测评标题不能为空")
    private String evaluationTitle;

    @NotBlank(message = "性能测试数据不能为空")
    private String perfTestData;

    @NotBlank(message = "使用体验不能为空")
    private String usageExperience;

    private String prosAndCons;

    private List<String> imageUrls;  // 图片URL列表
}