package com.pcadvisor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pcadvisor.common.Result;
import com.pcadvisor.entity.*;
import com.pcadvisor.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RankingController {

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

    @GetMapping("/ranking")
    public Result<Map<String, Object>> getRanking() {
        // 加载图片
        List<MediaInf> allMedia = mediaInfMapper.selectList(
            new QueryWrapper<MediaInf>().isNotNull("hardware_id")
        );
        Map<Integer, String> imageMap = allMedia.stream()
            .filter(m -> m.getHardwareId() != null)
            .collect(Collectors.toMap(
                MediaInf::getHardwareId,
                MediaInf::getMediaUrl,
                (a, b) -> a
            ));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cpuRanking", buildCpuRanking(imageMap));
        result.put("gpuRanking", buildGpuRanking(imageMap));
        result.put("mbRanking", buildMbRanking(imageMap));
        return Result.success(result);
    }

    private List<Map<String, Object>> buildCpuRanking(Map<Integer, String> imageMap) {
        List<HardwareInf> hwList = hardwareInfMapper.selectList(
            new QueryWrapper<HardwareInf>()
                .eq("hardware_type", "CPU")
                .eq("audit_state", "approved")
        );
        Map<Integer, CpuInf> detailMap = cpuInfMapper.selectList(null).stream()
            .collect(Collectors.toMap(CpuInf::getHardwareId, c -> c));

        List<Map<String, Object>> list = new ArrayList<>();
        for (HardwareInf hw : hwList) {
            CpuInf detail = detailMap.get(hw.getHardwareId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hardwareId", hw.getHardwareId());
            item.put("hardwareName", hw.getHardwareName());
            item.put("hardwareBrand", hw.getHardwareBrand());
            item.put("hardwarePrice", hw.getHardwarePrice());
            item.put("imageUrl", imageMap.get(hw.getHardwareId()));
            item.put("coreCount", detail != null ? detail.getCoreCount() : null);
            item.put("threadCount", detail != null ? detail.getThreadCount() : null);
            item.put("baseFreq", detail != null ? detail.getBaseFreq() : null);
            item.put("interfaceType", detail != null ? detail.getInterfaceType() : null);
            item.put("tdpPower", detail != null ? detail.getTdpPower() : null);
            // 性价比分: cores * freq / price * 1000
            double score = 0;
            if (detail != null && detail.getCoreCount() != null && detail.getBaseFreq() != null
                && hw.getHardwarePrice() != null && hw.getHardwarePrice().doubleValue() > 0) {
                score = detail.getCoreCount() * detail.getBaseFreq().doubleValue()
                    / hw.getHardwarePrice().doubleValue() * 1000;
            }
            item.put("score", Math.round(score * 10) / 10.0);
            list.add(item);
        }
        list.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return list;
    }

    private List<Map<String, Object>> buildGpuRanking(Map<Integer, String> imageMap) {
        List<HardwareInf> hwList = hardwareInfMapper.selectList(
            new QueryWrapper<HardwareInf>()
                .eq("hardware_type", "GRAPHICS_CARD")
                .eq("audit_state", "approved")
        );
        Map<Integer, GraphicsCardInf> detailMap = graphicsCardInfMapper.selectList(null).stream()
            .collect(Collectors.toMap(GraphicsCardInf::getHardwareId, g -> g));

        List<Map<String, Object>> list = new ArrayList<>();
        for (HardwareInf hw : hwList) {
            GraphicsCardInf detail = detailMap.get(hw.getHardwareId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hardwareId", hw.getHardwareId());
            item.put("hardwareName", hw.getHardwareName());
            item.put("hardwareBrand", hw.getHardwareBrand());
            item.put("hardwarePrice", hw.getHardwarePrice());
            item.put("imageUrl", imageMap.get(hw.getHardwareId()));
            item.put("coreModel", detail != null ? detail.getCoreModel() : null);
            item.put("vramCap", detail != null ? detail.getVramCap() : null);
            item.put("vramType", detail != null ? detail.getVramType() : null);
            item.put("powerConsump", detail != null ? detail.getPowerConsump() : null);
            // 性价比分：VRAM(GB) / price * 10000
            double score = 0;
            if (detail != null && detail.getVramCap() != null && hw.getHardwarePrice() != null
                && hw.getHardwarePrice().doubleValue() > 0) {
                try {
                    double vram = Double.parseDouble(detail.getVramCap().replaceAll("[^0-9.]", ""));
                    score = vram / hw.getHardwarePrice().doubleValue() * 10000;
                } catch (NumberFormatException ignored) {}
            }
            item.put("score", Math.round(score * 10) / 10.0);
            list.add(item);
        }
        list.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return list;
    }

    private List<Map<String, Object>> buildMbRanking(Map<Integer, String> imageMap) {
        List<HardwareInf> hwList = hardwareInfMapper.selectList(
            new QueryWrapper<HardwareInf>()
                .eq("hardware_type", "MOTHERBOARD")
                .eq("audit_state", "approved")
        );
        Map<Integer, MotherboardInf> detailMap = motherboardInfMapper.selectList(null).stream()
            .collect(Collectors.toMap(MotherboardInf::getHardwareId, m -> m));

        List<Map<String, Object>> list = new ArrayList<>();
        for (HardwareInf hw : hwList) {
            MotherboardInf detail = detailMap.get(hw.getHardwareId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hardwareId", hw.getHardwareId());
            item.put("hardwareName", hw.getHardwareName());
            item.put("hardwareBrand", hw.getHardwareBrand());
            item.put("hardwarePrice", hw.getHardwarePrice());
            item.put("imageUrl", imageMap.get(hw.getHardwareId()));
            item.put("cpuInterface", detail != null ? detail.getCpuInterface() : null);
            item.put("mbForm", detail != null ? detail.getMbForm() : null);
            item.put("memSlotCount", detail != null ? detail.getMemSlotCount() : null);
            item.put("m2SlotCount", detail != null ? detail.getM2SlotCount() : null);
            // 综合分：slot数/price * 10000
            double score = 0;
            if (detail != null && hw.getHardwarePrice() != null
                && hw.getHardwarePrice().doubleValue() > 0) {
                int slots = (detail.getMemSlotCount() != null ? detail.getMemSlotCount() : 0)
                    + (detail.getM2SlotCount() != null ? detail.getM2SlotCount() : 0);
                score = (double) slots / hw.getHardwarePrice().doubleValue() * 10000;
            }
            item.put("score", Math.round(score * 10) / 10.0);
            list.add(item);
        }
        list.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return list;
    }
}
