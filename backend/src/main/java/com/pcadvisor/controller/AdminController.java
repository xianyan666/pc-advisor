package com.pcadvisor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pcadvisor.common.Result;
import com.pcadvisor.common.enums.ErrorCode;
import com.pcadvisor.common.exception.BusinessException;
import com.pcadvisor.entity.*;
import com.pcadvisor.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private HardwareInfMapper hardwareInfMapper;
    @Autowired private CpuInfMapper cpuInfMapper;
    @Autowired private GraphicsCardInfMapper graphicsCardInfMapper;
    @Autowired private MotherboardInfMapper motherboardInfMapper;
    @Autowired private EvaluationMapper evaluationMapper;
    @Autowired private MediaInfMapper mediaInfMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private CommentMapper commentMapper;

    // ==================== 硬件管理 ====================

    @GetMapping("/hardware")
    public Result<List<Map<String, Object>>> listHardware() {
        List<HardwareInf> list = hardwareInfMapper.selectList(
            new QueryWrapper<HardwareInf>().orderByDesc("publish_time"));
        List<MediaInf> allMedia = mediaInfMapper.selectList(
            new QueryWrapper<MediaInf>().isNotNull("hardware_id"));
        Map<Integer, String> imgMap = allMedia.stream()
            .filter(m -> m.getHardwareId() != null)
            .collect(Collectors.toMap(MediaInf::getHardwareId, MediaInf::getMediaUrl, (a, b) -> a));

        // 详情表
        Map<Integer, CpuInf> cpuMap = cpuInfMapper.selectList(null).stream()
            .collect(Collectors.toMap(CpuInf::getHardwareId, c -> c));
        Map<Integer, GraphicsCardInf> gpuMap = graphicsCardInfMapper.selectList(null).stream()
            .collect(Collectors.toMap(GraphicsCardInf::getHardwareId, g -> g));
        Map<Integer, MotherboardInf> mbMap = motherboardInfMapper.selectList(null).stream()
            .collect(Collectors.toMap(MotherboardInf::getHardwareId, m -> m));

        List<Map<String, Object>> result = new ArrayList<>();
        for (HardwareInf hw : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("hardwareId", hw.getHardwareId());
            item.put("hardwareName", hw.getHardwareName());
            item.put("hardwareType", hw.getHardwareType());
            item.put("hardwareBrand", hw.getHardwareBrand());
            item.put("hardwareModel", hw.getHardwareModel());
            item.put("hardwarePrice", hw.getHardwarePrice());
            item.put("auditState", hw.getAuditState());
            item.put("publishTime", hw.getPublishTime());
            item.put("imageUrl", imgMap.get(hw.getHardwareId()));

            // 附加详情字段
            CpuInf cpu = cpuMap.get(hw.getHardwareId());
            if (cpu != null) {
                item.put("coreCount", cpu.getCoreCount());
                item.put("threadCount", cpu.getThreadCount());
                item.put("baseFreq", cpu.getBaseFreq());
                item.put("interfaceType", cpu.getInterfaceType());
                item.put("tdpPower", cpu.getTdpPower());
                item.put("supportMemType", cpu.getSupportMemType());
            }
            GraphicsCardInf gpu = gpuMap.get(hw.getHardwareId());
            if (gpu != null) {
                item.put("coreModel", gpu.getCoreModel());
                item.put("vramCap", gpu.getVramCap());
                item.put("vramType", gpu.getVramType());
                item.put("powerConsump", gpu.getPowerConsump());
                item.put("gcLength", gpu.getGcLength());
            }
            MotherboardInf mb = mbMap.get(hw.getHardwareId());
            if (mb != null) {
                item.put("cpuInterface", mb.getCpuInterface());
                item.put("mbForm", mb.getMbForm());
                item.put("memSlotCount", mb.getMemSlotCount());
                item.put("mbSupportMemType", mb.getSupportMemType());
                item.put("m2SlotCount", mb.getM2SlotCount());
            }
            result.add(item);
        }
        return Result.success(result);
    }

    @PostMapping("/hardware")
    public Result<Map<String, Object>> createHardware(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("hardwareName");
        String type = (String) body.get("hardwareType");
        String brand = (String) body.get("hardwareBrand");
        String model = (String) body.get("hardwareModel");
        Object priceObj = body.get("hardwarePrice");
        if (name == null || name.isBlank() || type == null || type.isBlank()) {
            throw new BusinessException("硬件名称和类型不能为空");
        }

        HardwareInf hw = new HardwareInf();
        hw.setHardwareName(name.trim());
        hw.setHardwareType(type);
        hw.setHardwareBrand(brand);
        hw.setHardwareModel(model);
        hw.setHardwarePrice(priceObj != null ? new java.math.BigDecimal(priceObj.toString()) : null);
        hw.setAuditState("approved");
        hw.setPublishTime(LocalDateTime.now());
        hardwareInfMapper.insert(hw);

        // 保存详情
        Map<String, Object> details = (Map<String, Object>) body.get("details");
        if (details != null) {
            if ("CPU".equals(type)) {
                CpuInf cpu = new CpuInf();
                cpu.setHardwareId(hw.getHardwareId());
                cpu.setCoreCount(toInt(details.get("coreCount")));
                cpu.setThreadCount(toInt(details.get("threadCount")));
                cpu.setBaseFreq(toDecimal(details.get("baseFreq")));
                cpu.setInterfaceType((String) details.get("interfaceType"));
                cpu.setTdpPower(toInt(details.get("tdpPower")));
                cpu.setSupportMemType((String) details.get("supportMemType"));
                cpuInfMapper.insert(cpu);
            } else if ("GRAPHICS_CARD".equals(type)) {
                GraphicsCardInf gpu = new GraphicsCardInf();
                gpu.setHardwareId(hw.getHardwareId());
                gpu.setCoreModel((String) details.get("coreModel"));
                gpu.setVramCap((String) details.get("vramCap"));
                gpu.setVramType((String) details.get("vramType"));
                gpu.setPowerConsump(toInt(details.get("powerConsump")));
                gpu.setGcLength(toInt(details.get("gcLength")));
                graphicsCardInfMapper.insert(gpu);
            } else if ("MOTHERBOARD".equals(type)) {
                MotherboardInf mb = new MotherboardInf();
                mb.setHardwareId(hw.getHardwareId());
                mb.setCpuInterface((String) details.get("cpuInterface"));
                mb.setMbForm((String) details.get("mbForm"));
                mb.setMemSlotCount(toInt(details.get("memSlotCount")));
                mb.setSupportMemType((String) details.get("supportMemType"));
                mb.setM2SlotCount(toInt(details.get("m2SlotCount")));
                motherboardInfMapper.insert(mb);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hardwareId", hw.getHardwareId());
        result.put("message", "硬件添加成功");
        return Result.success(result);
    }

    @PutMapping("/hardware/{id}")
    public Result<Map<String, Object>> updateHardware(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        HardwareInf hw = hardwareInfMapper.selectById(id);
        if (hw == null) throw new BusinessException(ErrorCode.HARDWARE_NOT_FOUND);

        if (body.containsKey("hardwareName")) hw.setHardwareName((String) body.get("hardwareName"));
        if (body.containsKey("hardwareBrand")) hw.setHardwareBrand((String) body.get("hardwareBrand"));
        if (body.containsKey("hardwareModel")) hw.setHardwareModel((String) body.get("hardwareModel"));
        if (body.containsKey("hardwarePrice")) hw.setHardwarePrice(new java.math.BigDecimal(body.get("hardwarePrice").toString()));
        if (body.containsKey("auditState")) hw.setAuditState((String) body.get("auditState"));
        hw.setUpdateTime(LocalDateTime.now());
        hardwareInfMapper.updateById(hw);

        // 更新详情表
        String type = hw.getHardwareType();
        if ("CPU".equals(type)) {
            CpuInf cpu = cpuInfMapper.selectOne(new QueryWrapper<CpuInf>().eq("hardware_id", id));
            if (cpu != null) {
                if (body.containsKey("coreCount")) cpu.setCoreCount(toInt(body.get("coreCount")));
                if (body.containsKey("threadCount")) cpu.setThreadCount(toInt(body.get("threadCount")));
                if (body.containsKey("baseFreq")) cpu.setBaseFreq(toDecimal(body.get("baseFreq")));
                if (body.containsKey("interfaceType")) cpu.setInterfaceType((String) body.get("interfaceType"));
                if (body.containsKey("tdpPower")) cpu.setTdpPower(toInt(body.get("tdpPower")));
                if (body.containsKey("supportMemType")) cpu.setSupportMemType((String) body.get("supportMemType"));
                cpuInfMapper.updateById(cpu);
            }
        } else if ("GRAPHICS_CARD".equals(type)) {
            GraphicsCardInf gpu = graphicsCardInfMapper.selectOne(new QueryWrapper<GraphicsCardInf>().eq("hardware_id", id));
            if (gpu != null) {
                if (body.containsKey("coreModel")) gpu.setCoreModel((String) body.get("coreModel"));
                if (body.containsKey("vramCap")) gpu.setVramCap((String) body.get("vramCap"));
                if (body.containsKey("vramType")) gpu.setVramType((String) body.get("vramType"));
                if (body.containsKey("powerConsump")) gpu.setPowerConsump(toInt(body.get("powerConsump")));
                if (body.containsKey("gcLength")) gpu.setGcLength(toInt(body.get("gcLength")));
                graphicsCardInfMapper.updateById(gpu);
            }
        } else if ("MOTHERBOARD".equals(type)) {
            MotherboardInf mb = motherboardInfMapper.selectOne(new QueryWrapper<MotherboardInf>().eq("hardware_id", id));
            if (mb != null) {
                if (body.containsKey("cpuInterface")) mb.setCpuInterface((String) body.get("cpuInterface"));
                if (body.containsKey("mbForm")) mb.setMbForm((String) body.get("mbForm"));
                if (body.containsKey("memSlotCount")) mb.setMemSlotCount(toInt(body.get("memSlotCount")));
                if (body.containsKey("supportMemType")) mb.setSupportMemType((String) body.get("supportMemType"));
                if (body.containsKey("m2SlotCount")) mb.setM2SlotCount(toInt(body.get("m2SlotCount")));
                motherboardInfMapper.updateById(mb);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "硬件更新成功");
        return Result.success(result);
    }

    @DeleteMapping("/hardware/{id}")
    public Result<Map<String, Object>> deleteHardware(@PathVariable Integer id) {
        if (hardwareInfMapper.selectById(id) == null) throw new BusinessException(ErrorCode.HARDWARE_NOT_FOUND);
        hardwareInfMapper.deleteById(id);
        // 清除关联图片
        mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("hardware_id", id));
        Map<String, Object> result = new HashMap<>();
        result.put("message", "硬件已删除");
        return Result.success(result);
    }

    // ==================== 评测审核 ====================

    @GetMapping("/evaluations")
    public Result<List<Map<String, Object>>> listEvaluations(
            @RequestParam(defaultValue = "pending") String state) {
        List<Evaluation> list = evaluationMapper.selectList(
            new QueryWrapper<Evaluation>()
                .eq("audit_state", state)
                .orderByDesc("publish_time"));

        Set<Integer> evalIds = list.stream().map(Evaluation::getEvaluationId).collect(Collectors.toSet());
        Map<Integer, List<MediaInf>> imgMap = Collections.emptyMap();
        if (!evalIds.isEmpty()) {
            imgMap = mediaInfMapper.selectList(new QueryWrapper<MediaInf>().in("evaluation_id", evalIds))
                .stream().filter(m -> m.getEvaluationId() != null)
                .collect(Collectors.groupingBy(MediaInf::getEvaluationId));
        }

        Set<String> userIds = list.stream().map(Evaluation::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectList(new QueryWrapper<User>().in("user_id", userIds))
                .stream().collect(Collectors.toMap(User::getUserId, User::getUsername));
        }

        // 关联硬件
        Set<Integer> hwIds = list.stream().map(Evaluation::getHardwareId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, HardwareInf> hwMap = Collections.emptyMap();
        if (!hwIds.isEmpty()) {
            hwMap = hardwareInfMapper.selectList(new QueryWrapper<HardwareInf>().in("HardwareID", hwIds))
                .stream().collect(Collectors.toMap(HardwareInf::getHardwareId, h -> h));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Evaluation e : list) {
            HardwareInf hw = hwMap.get(e.getHardwareId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("evaluationId", e.getEvaluationId());
            item.put("evaluationTitle", e.getEvaluationTitle());
            item.put("auditState", e.getAuditState());
            item.put("publishTime", e.getPublishTime());
            item.put("perfTestData", e.getPerfTestData());
            item.put("usageExperience", e.getUsageExperience());
            item.put("prosAndCons", e.getProsAndCons());
            item.put("hardwareId", e.getHardwareId());
            item.put("publisherName", userMap.getOrDefault(e.getUserId(), "匿名"));
            if (hw != null) {
                item.put("hardwareName", hw.getHardwareName());
                item.put("hardwareType", hw.getHardwareType());
            }
            List<MediaInf> imgs = imgMap.getOrDefault(e.getEvaluationId(), Collections.emptyList());
            item.put("images", imgs.stream().map(MediaInf::getMediaUrl).collect(Collectors.toList()));
            result.add(item);
        }
        return Result.success(result);
    }

    @PutMapping("/evaluation/{id}/audit")
    public Result<Map<String, Object>> auditEvaluation(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Evaluation e = evaluationMapper.selectById(id);
        if (e == null) throw new BusinessException("评测不存在");
        String action = body.get("action"); // approved / rejected
        if (!"approved".equals(action) && !"rejected".equals(action)) {
            throw new BusinessException("审核操作无效，必须为 approved 或 rejected");
        }
        e.setAuditState(action);
        e.setAuditTime(LocalDateTime.now());
        evaluationMapper.updateById(e);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "审核完成");
        return Result.success(result);
    }

    // ==================== 评论审核 ====================

    @GetMapping("/comments")
    public Result<List<Map<String, Object>>> listComments(
            @RequestParam(defaultValue = "pending") String state) {
        List<Comment> list = commentMapper.selectList(
            new QueryWrapper<Comment>()
                .eq("audit_state", state)
                .orderByDesc("publish_time"));

        Set<Integer> evalIds = list.stream().map(Comment::getEvaluationId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, String> evalTitleMap = Collections.emptyMap();
        if (!evalIds.isEmpty()) {
            evalTitleMap = evaluationMapper.selectList(new QueryWrapper<Evaluation>().in("EvaluationID", evalIds))
                .stream().collect(Collectors.toMap(Evaluation::getEvaluationId, Evaluation::getEvaluationTitle));
        }

        Set<String> userIds = list.stream().map(Comment::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, String> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectList(new QueryWrapper<User>().in("user_id", userIds))
                .stream().collect(Collectors.toMap(User::getUserId, User::getUsername));
        }

        Set<String> commentIds = list.stream().map(Comment::getCommentId).collect(Collectors.toSet());
        Map<String, List<MediaInf>> imgMap = Collections.emptyMap();
        if (!commentIds.isEmpty()) {
            imgMap = mediaInfMapper.selectList(new QueryWrapper<MediaInf>().in("comment_id", commentIds))
                .stream().filter(m -> m.getCommentId() != null)
                .collect(Collectors.groupingBy(MediaInf::getCommentId));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commentId", c.getCommentId());
            item.put("evaluationId", c.getEvaluationId());
            item.put("evaluationTitle", evalTitleMap.getOrDefault(c.getEvaluationId(), "(已删除)"));
            item.put("content", c.getContent());
            item.put("auditState", c.getAuditState());
            item.put("publishTime", c.getPublishTime());
            item.put("userName", userMap.getOrDefault(c.getUserId(), "匿名"));
            List<MediaInf> imgs = imgMap.getOrDefault(c.getCommentId(), Collections.emptyList());
            item.put("images", imgs.stream().map(MediaInf::getMediaUrl).collect(Collectors.toList()));
            result.add(item);
        }
        return Result.success(result);
    }

    @PutMapping("/comment/{id}/audit")
    public Result<Map<String, Object>> auditComment(@PathVariable String id, @RequestBody Map<String, String> body) {
        Comment c = commentMapper.selectById(id);
        if (c == null) throw new BusinessException("评论不存在");
        String action = body.get("action");
        if (!"approved".equals(action) && !"rejected".equals(action)) {
            throw new BusinessException("审核操作无效，必须为 approved 或 rejected");
        }
        c.setAuditState(action);
        c.setAuditTime(LocalDateTime.now());
        commentMapper.updateById(c);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "审核完成");
        return Result.success(result);
    }

    @DeleteMapping("/evaluation/{id}")
    public Result<Map<String, Object>> deleteEvaluation(@PathVariable Integer id) {
        Evaluation eval = evaluationMapper.selectById(id);
        if (eval == null) throw new BusinessException("评测不存在");

        // 删除关联评论及其图片
        List<Comment> comments = commentMapper.selectList(
            new QueryWrapper<Comment>().eq("evaluation_id", id));
        for (Comment c : comments) {
            mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("comment_id", c.getCommentId()));
        }
        commentMapper.delete(new QueryWrapper<Comment>().eq("evaluation_id", id));

        // 删除评测关联图片
        mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("evaluation_id", id));

        evaluationMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "评测已删除");
        return Result.success(result);
    }

    @DeleteMapping("/comment/{id}")
    public Result<Map<String, Object>> deleteComment(@PathVariable String id) {
        Comment c = commentMapper.selectById(id);
        if (c == null) throw new BusinessException("评论不存在");

        mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("comment_id", id));
        commentMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "评论已删除");
        return Result.success(result);
    }

    // ==================== 统计 ====================

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("hardwareCount", hardwareInfMapper.selectCount(null));
        s.put("cpuCount", hardwareInfMapper.selectCount(new QueryWrapper<HardwareInf>().eq("hardware_type", "CPU")));
        s.put("gpuCount", hardwareInfMapper.selectCount(new QueryWrapper<HardwareInf>().eq("hardware_type", "GRAPHICS_CARD")));
        s.put("mbCount", hardwareInfMapper.selectCount(new QueryWrapper<HardwareInf>().eq("hardware_type", "MOTHERBOARD")));
        s.put("evalCount", evaluationMapper.selectCount(null));
        s.put("pendingEvalCount", evaluationMapper.selectCount(new QueryWrapper<Evaluation>().eq("audit_state", "pending")));
        s.put("commentCount", commentMapper.selectCount(null));
        s.put("pendingCommentCount", commentMapper.selectCount(new QueryWrapper<Comment>().eq("audit_state", "pending")));
        s.put("userCount", userMapper.selectCount(null));
        return Result.success(s);
    }

    private Integer toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) try { return Integer.parseInt((String) v); } catch (NumberFormatException e) {}
        return null;
    }

    private java.math.BigDecimal toDecimal(Object v) {
        if (v instanceof Number) return new java.math.BigDecimal(v.toString());
        if (v instanceof String) try { return new java.math.BigDecimal((String) v); } catch (NumberFormatException e) {}
        return null;
    }
}
