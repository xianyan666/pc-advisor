package com.pcadvisor.controller;

import com.pcadvisor.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db")
    public Result<String> testDatabase() {
        try {
            String result = jdbcTemplate.queryForObject("SELECT '数据库连接成功！'", String.class);
            return Result.success("✅ " + result);
        } catch (Exception e) {
            return Result.error("❌ 数据库连接失败：" + e.getMessage());
        }
    }

    @GetMapping("/tables")
    public Result<List<String>> showTables() {
        String sql = "SHOW TABLES";
        return Result.success(jdbcTemplate.queryForList(sql, String.class));
    }

    @GetMapping("/users")
    public Result<List<Map<String, Object>>> showUsers() {
        String sql = "SELECT * FROM sys_user";
        return Result.success(jdbcTemplate.queryForList(sql));
    }

    @GetMapping("/hardware")
    public Result<List<Map<String, Object>>> showHardware() {
        try {
            String sql = """
                SELECT h.HardwareID,
                       h.user_id,
                       h.hardware_name AS HardwareName,
                       h.hardware_type AS HardwareType,
                       h.hardware_brand AS HardwareBrand,
                       h.hardware_model AS HardwareModel,
                       h.hardware_price AS HardwarePrice,
                       h.audit_state AS auditState,
                       h.publish_time AS publishTime,
                       h.audit_time AS auditTime,
                       h.update_time AS updateTime,
                       m.media_url AS imageUrl,
                       c.core_count AS coreCount, c.thread_count AS threadCount,
                       c.base_freq AS baseFreq, c.interface_type AS cpuInterface,
                       c.tdp_power AS tdpPower, c.support_mem_type AS cpuMemType,
                       g.core_model AS coreModel, g.vram_cap AS vramCap,
                       g.vram_type AS vramType, g.power_consump AS powerConsump,
                       g.gc_length AS gcLength,
                       mb.cpu_interface AS mbCpuInterface, mb.mb_form AS mbForm,
                       mb.mem_slot_count AS memSlotCount, mb.support_mem_type AS mbMemType,
                       mb.m2_slot_count AS m2SlotCount,
                       (SELECT COUNT(*) FROM Evaluation WHERE hardware_id = h.HardwareID) AS evaluationCount
                FROM Hardware_Inf h
                LEFT JOIN Media_Inf m ON h.HardwareID = m.hardware_id
                LEFT JOIN CPU_Inf c ON h.HardwareID = c.hardware_id
                LEFT JOIN GraphicsCard_Inf g ON h.HardwareID = g.hardware_id
                LEFT JOIN Motherboard_Inf mb ON h.HardwareID = mb.hardware_id
                """;
            return Result.success(jdbcTemplate.queryForList(sql));
        } catch (Exception e) {
            String fallbackSql = """
                SELECT h.HardwareID,
                       h.user_id,
                       h.hardware_name AS HardwareName,
                       h.hardware_type AS HardwareType,
                       h.hardware_brand AS HardwareBrand,
                       h.hardware_model AS HardwareModel,
                       h.hardware_price AS HardwarePrice,
                       h.audit_state AS auditState,
                       h.publish_time AS publishTime,
                       m.media_url AS imageUrl,
                       (SELECT COUNT(*) FROM Evaluation WHERE hardware_id = h.HardwareID) AS evaluationCount
                FROM Hardware_Inf h
                LEFT JOIN Media_Inf m ON h.HardwareID = m.hardware_id
                """;
            try {
                return Result.success(jdbcTemplate.queryForList(fallbackSql));
            } catch (Exception ex) {
                return Result.success(java.util.List.of());
            }
        }
    }
}