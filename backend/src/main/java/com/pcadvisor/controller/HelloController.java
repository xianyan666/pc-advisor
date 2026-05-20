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
public class HelloController {

    @Autowired
    private HardwareInfMapper hardwareInfMapper;

    @Autowired
    private MediaInfMapper mediaInfMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/hello")
    public Result<String> sayHello() {
        return Result.success("Hello! 电脑硬件系统启动成功！");
    }

    @GetMapping("/hardware")
    public Result<List<Map<String, Object>>> getHardware() {
        List<HardwareInf> hardwareList = hardwareInfMapper.selectList(null);
        List<MediaInf> allMedia = mediaInfMapper.selectList(
            new QueryWrapper<MediaInf>().isNotNull("hardware_id")
        );
        Map<Integer, List<MediaInf>> mediaMap = allMedia.stream()
            .filter(m -> m.getHardwareId() != null)
            .collect(Collectors.groupingBy(MediaInf::getHardwareId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (HardwareInf hw : hardwareList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hardwareId", hw.getHardwareId());
            item.put("hardwareName", hw.getHardwareName());
            item.put("hardwareType", hw.getHardwareType());
            item.put("hardwareBrand", hw.getHardwareBrand());
            item.put("hardwareModel", hw.getHardwareModel());
            item.put("hardwarePrice", hw.getHardwarePrice());
            item.put("auditState", hw.getAuditState());

            List<MediaInf> images = mediaMap.get(hw.getHardwareId());
            if (images != null && !images.isEmpty()) {
                item.put("imageUrl", images.get(0).getMediaUrl());
            } else {
                item.put("imageUrl", null);
            }
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 获取所有已通过的评测列表（公开）
     */
    @GetMapping("/evaluations")
    public Result<List<Map<String, Object>>> getAllEvaluations() {
        List<Evaluation> evaluations = evaluationMapper.selectList(
            new QueryWrapper<Evaluation>()
                .eq("audit_state", "approved")
                .orderByDesc("publish_time")
        );

        if (evaluations.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<Integer> hardwareIds = evaluations.stream()
            .map(Evaluation::getHardwareId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Integer, HardwareInf> hardwareMap = Collections.emptyMap();
        Map<Integer, List<MediaInf>> hwMediaMap = Collections.emptyMap();
        if (!hardwareIds.isEmpty()) {
            hardwareMap = hardwareInfMapper.selectList(
                new QueryWrapper<HardwareInf>().in("HardwareID", hardwareIds)
            ).stream().collect(Collectors.toMap(HardwareInf::getHardwareId, h -> h));

            hwMediaMap = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("hardware_id", hardwareIds)
            ).stream().filter(m -> m.getHardwareId() != null)
              .collect(Collectors.groupingBy(MediaInf::getHardwareId));
        }

        Set<Integer> evalIds = evaluations.stream()
            .map(Evaluation::getEvaluationId)
            .collect(Collectors.toSet());
        Map<Integer, List<MediaInf>> evalMediaMap = Collections.emptyMap();
        if (!evalIds.isEmpty()) {
            evalMediaMap = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("evaluation_id", evalIds)
            ).stream().filter(m -> m.getEvaluationId() != null)
              .collect(Collectors.groupingBy(MediaInf::getEvaluationId));
        }

        Set<String> userIds = evaluations.stream()
            .map(Evaluation::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<String, String> userNameMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userNameMap = userMapper.selectList(
                new QueryWrapper<User>().in("user_id", userIds)
            ).stream().collect(Collectors.toMap(User::getUserId, User::getUsername));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Evaluation eval : evaluations) {
            HardwareInf hw = hardwareMap.get(eval.getHardwareId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("evaluationId", eval.getEvaluationId());
            item.put("evaluationTitle", eval.getEvaluationTitle());
            item.put("publishTime", eval.getPublishTime());
            item.put("usageExperience", eval.getUsageExperience());
            item.put("publisherName", userNameMap.getOrDefault(eval.getUserId(), "匿名用户"));

            List<MediaInf> evalImgs = evalMediaMap.getOrDefault(eval.getEvaluationId(), Collections.emptyList());
            item.put("images", evalImgs.stream().map(MediaInf::getMediaUrl).collect(Collectors.toList()));

            if (hw != null) {
                Map<String, Object> hwInfo = new LinkedHashMap<>();
                hwInfo.put("hardwareId", hw.getHardwareId());
                hwInfo.put("hardwareName", hw.getHardwareName());
                hwInfo.put("hardwareType", hw.getHardwareType());
                hwInfo.put("hardwareBrand", hw.getHardwareBrand());
                hwInfo.put("hardwarePrice", hw.getHardwarePrice());

                List<MediaInf> hwImgs = hwMediaMap.get(hw.getHardwareId());
                hwInfo.put("imageUrl", (hwImgs != null && !hwImgs.isEmpty()) ? hwImgs.get(0).getMediaUrl() : null);

                item.put("hardware", hwInfo);
            } else {
                item.put("hardware", null);
            }

            result.add(item);
        }

        return Result.success(result);
    }
}