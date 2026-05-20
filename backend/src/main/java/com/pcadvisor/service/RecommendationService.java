package com.pcadvisor.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pcadvisor.entity.*;
import com.pcadvisor.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private HardwareInfMapper hardwareInfMapper;

    @Autowired
    private CpuInfMapper cpuInfMapper;

    @Autowired
    private GraphicsCardInfMapper graphicsCardInfMapper;

    @Autowired
    private MotherboardInfMapper motherboardInfMapper;

    @Autowired
    private MediaInfMapper mediaInfMapper;

    public RecommendationResult recommend(RecommendInput input) {
        // 1. 加载所有硬件
        List<HardwareInf> allHw = hardwareInfMapper.selectList(
            new QueryWrapper<HardwareInf>().eq("audit_state", "approved")
        );
        Map<Integer, HardwareInf> hwMap = allHw.stream()
            .collect(Collectors.toMap(HardwareInf::getHardwareId, h -> h));

        // CPU列表
        List<CpuInf> allCpus = cpuInfMapper.selectList(null);
        // GPU列表
        List<GraphicsCardInf> allGpus = graphicsCardInfMapper.selectList(null);
        // 主板列表
        List<MotherboardInf> allMbs = motherboardInfMapper.selectList(null);

        // 构建兼容组合
        List<Combo> combos = new ArrayList<>();
        for (MotherboardInf mb : allMbs) {
            HardwareInf mbHw = hwMap.get(mb.getHardwareId());
            if (mbHw == null) continue;

            for (CpuInf cpu : allCpus) {
                // CPU接口与主板接口匹配
                if (!mb.getCpuInterface().equalsIgnoreCase(cpu.getInterfaceType())) continue;

                HardwareInf cpuHw = hwMap.get(cpu.getHardwareId());
                if (cpuHw == null) continue;

                // 游戏配置必须有独显
                if (input.getIsGaming() != null && input.getIsGaming()) {
                    for (GraphicsCardInf gpu : allGpus) {
                        HardwareInf gpuHw = hwMap.get(gpu.getHardwareId());
                        if (gpuHw == null) continue;
                        addComboIfValid(combos, cpuHw, cpu, gpuHw, gpu, mbHw, mb, input);
                    }
                } else {
                    // 非游戏：允许核显
                    addComboIfValid(combos, cpuHw, cpu, null, null, mbHw, mb, input);
                }
            }
        }

        // 2. 硬性筛选结果
        if (combos.isEmpty()) {
            RecommendationResult empty = new RecommendationResult();
            empty.setPassed(false);
            empty.setSuggestion(buildFailSuggestion(input));
            return empty;
        }

        // 3. 软性打分
        int totalSoft = countSoftConditions(input);
        List<ScoredCombo> scored = new ArrayList<>();
        for (Combo c : combos) {
            int hits = 0;
            List<String> hitDetails = new ArrayList<>();
            List<String> missDetails = new ArrayList<>();

            // GPU系列
            if (input.getPreferredGpuSeries() != null && !input.getPreferredGpuSeries().isBlank()) {
                if (c.gpuSeries != null && matchGpuSeries(c.gpuSeries, input.getPreferredGpuSeries())) {
                    hits++;
                    hitDetails.add("显卡系列: " + input.getPreferredGpuSeries());
                } else {
                    missDetails.add("显卡系列");
                }
            }

            // 主板尺寸
            if (input.getPreferredMoboSize() != null && !input.getPreferredMoboSize().isBlank()) {
                if (input.getPreferredMoboSize().equalsIgnoreCase(c.mbForm)) {
                    hits++;
                    hitDetails.add("主板尺寸: " + input.getPreferredMoboSize());
                } else {
                    missDetails.add("主板尺寸");
                }
            }

            // 品牌偏好（匹配CPU/GPU/MB任意品牌）
            if (input.getPreferredBrands() != null && !input.getPreferredBrands().isEmpty()) {
                Set<String> comboBrands = new HashSet<>();
                if (c.cpuBrand != null) comboBrands.add(c.cpuBrand);
                if (c.gpuBrand != null) comboBrands.add(c.gpuBrand);
                if (c.mbBrand != null) comboBrands.add(c.mbBrand);
                boolean brandHit = input.getPreferredBrands().stream().anyMatch(comboBrands::contains);
                if (brandHit) {
                    hits++;
                    hitDetails.add("品牌偏好");
                } else {
                    missDetails.add("品牌偏好");
                }
            }

            double score = totalSoft > 0 ? (double) hits / totalSoft : 1.0;
            ScoredCombo sc = new ScoredCombo();
            sc.combo = c;
            sc.score = score;
            sc.hitCount = hits;
            sc.totalSoft = totalSoft;
            sc.hitDetails = hitDetails;
            sc.missDetails = missDetails;
            scored.add(sc);
        }

        // 4. 排序
        scored.sort(Comparator
            .comparingDouble(ScoredCombo::getScore).reversed()
            .thenComparing(sc -> sc.combo.totalPrice));

        // 5. Top N
        int topN = Math.min(3, scored.size());
        List<ScoredCombo> topResults = scored.subList(0, topN);

        // 加载图片
        Set<Integer> allHwIds = new HashSet<>();
        for (ScoredCombo sc : topResults) {
            allHwIds.add(sc.combo.cpuId);
            if (sc.combo.gpuId != null) allHwIds.add(sc.combo.gpuId);
            allHwIds.add(sc.combo.mbId);
        }
        Map<Integer, String> imageMap = new HashMap<>();
        if (!allHwIds.isEmpty()) {
            List<MediaInf> media = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("hardware_id", allHwIds)
            );
            for (MediaInf m : media) {
                if (m.getHardwareId() != null && !imageMap.containsKey(m.getHardwareId())) {
                    imageMap.put(m.getHardwareId(), m.getMediaUrl());
                }
            }
        }

        RecommendationResult result = new RecommendationResult();
        result.setPassed(true);
        result.setResults(topResults);
        result.setImageMap(imageMap);
        result.setTotalCandidates(scored.size());
        return result;
    }

    private void addComboIfValid(List<Combo> combos, HardwareInf cpuHw, CpuInf cpu,
                                  HardwareInf gpuHw, GraphicsCardInf gpu,
                                  HardwareInf mbHw, MotherboardInf mb,
                                  RecommendInput input) {
        BigDecimal total = cpuHw.getHardwarePrice() != null ? cpuHw.getHardwarePrice() : BigDecimal.ZERO;
        if (gpuHw != null && gpuHw.getHardwarePrice() != null) total = total.add(gpuHw.getHardwarePrice());
        if (mbHw.getHardwarePrice() != null) total = total.add(mbHw.getHardwarePrice());

        // 预算
        if (input.getBudget() != null && total.compareTo(input.getBudget()) > 0) return;
        // CPU核心数
        if (input.getMinCpuCores() != null && cpu.getCoreCount() != null
            && cpu.getCoreCount() < input.getMinCpuCores()) return;

        Combo c = new Combo();
        c.cpuId = cpuHw.getHardwareId();
        c.cpuName = cpuHw.getHardwareName();
        c.cpuBrand = cpuHw.getHardwareBrand();
        c.cpuCores = cpu.getCoreCount();
        c.cpuBaseFreq = cpu.getBaseFreq();
        c.cpuPrice = cpuHw.getHardwarePrice();

        if (gpuHw != null) {
            c.gpuId = gpuHw.getHardwareId();
            c.gpuName = gpuHw.getHardwareName();
            c.gpuBrand = gpuHw.getHardwareBrand();
            c.gpuSeries = classifyGpuSeries(gpu.getCoreModel());
            c.gpuVram = gpu.getVramCap();
            c.gpuPrice = gpuHw.getHardwarePrice();
        }

        c.mbId = mbHw.getHardwareId();
        c.mbName = mbHw.getHardwareName();
        c.mbBrand = mbHw.getHardwareBrand();
        c.mbForm = mb.getMbForm();
        c.mbCpuInterface = mb.getCpuInterface();
        c.mbPrice = mbHw.getHardwarePrice();

        c.totalPrice = total;
        combos.add(c);
    }

    /**
     * 根据显卡核心型号归类系列
     */
    private String classifyGpuSeries(String coreModel) {
        if (coreModel == null) return null;
        String m = coreModel.toUpperCase().replace(" ", "");
        // RTX 50系
        if (m.contains("GB202") || m.contains("GB203") || m.contains("GB205")) return "RTX50系";
        // RTX 40系 (AD102/103/104/106/107)
        if (m.contains("AD102") || m.contains("AD103") || m.contains("AD104")
            || m.contains("AD106") || m.contains("AD107")) return "RTX40系";
        // RTX 30系
        if (m.contains("GA102") || m.contains("GA104") || m.contains("GA106")) return "RTX30系";
        // AMD RX 7000系 (Navi 31/32/33 + Navi 48)
        if (m.contains("NAVI31") || m.contains("NAVI32") || m.contains("NAVI33")
            || m.contains("NAVI48")) return "RX7000系";
        return "其他系列";
    }

    private boolean matchGpuSeries(String configSeries, String userPreference) {
        if (configSeries == null) return false;
        String pref = userPreference.toLowerCase().replace(" ", "");
        String cfg = configSeries.toLowerCase().replace(" ", "");
        if (cfg.equals(pref)) return true;
        if (cfg.contains(pref) || pref.contains(cfg)) return true;
        if (pref.contains("rtx50") && cfg.contains("rtx50")) return true;
        if (pref.contains("rtx40") && cfg.contains("rtx40")) return true;
        if (pref.contains("rtx30") && cfg.contains("rtx30")) return true;
        if ((pref.contains("a卡") || pref.contains("radeon") || pref.contains("rx"))
            && (cfg.contains("rx") || cfg.contains("radeon"))) return true;
        return false;
    }

    private int countSoftConditions(RecommendInput input) {
        int count = 0;
        if (input.getPreferredGpuSeries() != null && !input.getPreferredGpuSeries().isBlank()) count++;
        if (input.getPreferredMoboSize() != null && !input.getPreferredMoboSize().isBlank()) count++;
        if (input.getPreferredBrands() != null && !input.getPreferredBrands().isEmpty()) count++;
        return count;
    }

    private String buildFailSuggestion(RecommendInput input) {
        return String.format(
            "未找到满足 CPU≥%d核、预算≤%s元%s的兼容配置。建议提高预算或降低CPU核心数要求。",
            input.getMinCpuCores() != null ? input.getMinCpuCores() : 0,
            input.getBudget() != null ? input.getBudget().toString() : "不限",
            input.getIsGaming() != null && input.getIsGaming() ? " 且 游戏门槛（需独显）" : ""
        );
    }

    // ==================== 内部类 ====================

    public static class Combo {
        public Integer cpuId, gpuId, mbId;
        public String cpuName, cpuBrand;
        public Integer cpuCores;
        public BigDecimal cpuBaseFreq, cpuPrice;
        public String gpuName, gpuBrand, gpuSeries, gpuVram;
        public BigDecimal gpuPrice;
        public String mbName, mbBrand, mbForm, mbCpuInterface;
        public BigDecimal mbPrice;
        public BigDecimal totalPrice;
    }

    public static class ScoredCombo {
        public Combo combo;
        public double score;
        public int hitCount;
        public int totalSoft;
        public List<String> hitDetails;
        public List<String> missDetails;

        public Combo getCombo() { return combo; }
        public double getScore() { return score; }
        public int getHitCount() { return hitCount; }
        public int getTotalSoft() { return totalSoft; }
        public List<String> getHitDetails() { return hitDetails; }
        public List<String> getMissDetails() { return missDetails; }
    }

    public static class RecommendationResult {
        private boolean passed;
        private String suggestion;
        private List<ScoredCombo> results;
        private Map<Integer, String> imageMap;
        private int totalCandidates;

        public boolean isPassed() { return passed; }
        public void setPassed(boolean v) { this.passed = v; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String v) { this.suggestion = v; }
        public List<ScoredCombo> getResults() { return results; }
        public void setResults(List<ScoredCombo> v) { this.results = v; }
        public Map<Integer, String> getImageMap() { return imageMap; }
        public void setImageMap(Map<Integer, String> v) { this.imageMap = v; }
        public int getTotalCandidates() { return totalCandidates; }
        public void setTotalCandidates(int v) { this.totalCandidates = v; }
    }

    // ==================== 输入 ====================

    public static class RecommendInput {
        private Boolean isGaming;
        private BigDecimal budget;
        private Integer minCpuCores;
        private String preferredGpuSeries;
        private String preferredMoboSize;
        private List<String> preferredBrands;

        public Boolean getIsGaming() { return isGaming; }
        public void setIsGaming(Boolean v) { this.isGaming = v; }
        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal v) { this.budget = v; }
        public Integer getMinCpuCores() { return minCpuCores; }
        public void setMinCpuCores(Integer v) { this.minCpuCores = v; }
        public String getPreferredGpuSeries() { return preferredGpuSeries; }
        public void setPreferredGpuSeries(String v) { this.preferredGpuSeries = v; }
        public String getPreferredMoboSize() { return preferredMoboSize; }
        public void setPreferredMoboSize(String v) { this.preferredMoboSize = v; }
        public List<String> getPreferredBrands() { return preferredBrands; }
        public void setPreferredBrands(List<String> v) { this.preferredBrands = v; }
    }
}
