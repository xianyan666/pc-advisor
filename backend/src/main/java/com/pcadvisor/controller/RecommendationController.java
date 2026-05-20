package com.pcadvisor.controller;

import com.pcadvisor.common.Result;
import com.pcadvisor.service.RecommendationService;
import com.pcadvisor.service.RecommendationService.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody RecommendInput input) {
        RecommendationResult result = recommendationService.recommend(input);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("passed", result.isPassed());
        response.put("totalCandidates", result.getTotalCandidates());
        response.put("imageMap", result.getImageMap());

        if (!result.isPassed()) {
            response.put("suggestion", result.getSuggestion());
            response.put("results", Collections.emptyList());
        } else {
            response.put("suggestion", null);
            List<Map<String, Object>> list = new ArrayList<>();
            for (ScoredCombo sc : result.getResults()) {
                Map<String, Object> item = new LinkedHashMap<>();
                Combo c = sc.getCombo();
                item.put("totalPrice", c.totalPrice);
                item.put("cpuId", c.cpuId);
                item.put("cpuName", c.cpuName);
                item.put("cpuBrand", c.cpuBrand);
                item.put("cpuCores", c.cpuCores);
                item.put("cpuBaseFreq", c.cpuBaseFreq);
                item.put("cpuPrice", c.cpuPrice);
                item.put("gpuId", c.gpuId);
                item.put("gpuName", c.gpuName);
                item.put("gpuBrand", c.gpuBrand);
                item.put("gpuSeries", c.gpuSeries);
                item.put("gpuVram", c.gpuVram);
                item.put("gpuPrice", c.gpuPrice);
                item.put("mbId", c.mbId);
                item.put("mbName", c.mbName);
                item.put("mbBrand", c.mbBrand);
                item.put("mbForm", c.mbForm);
                item.put("mbCpuInterface", c.mbCpuInterface);
                item.put("mbPrice", c.mbPrice);
                item.put("score", Math.round(sc.getScore() * 100));
                item.put("hitCount", sc.getHitCount());
                item.put("totalSoft", sc.getTotalSoft());
                item.put("hitDetails", sc.getHitDetails());
                item.put("missDetails", sc.getMissDetails());
                list.add(item);
            }
            response.put("results", list);
        }

        return Result.success(response);
    }
}
