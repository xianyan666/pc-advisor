// HardwareType.java - 硬件类型枚举
package com.pcadvisor.common.enums;

public enum HardwareType {
    CPU("CPU", "处理器"),
    MOTHERBOARD("Motherboard", "主板"),
    GRAPHICS_CARD("GraphicsCard", "显卡"),
    MEMORY("Memory", "内存"),
    STORAGE("Storage", "存储"),
    POWER_SUPPLY("PowerSupply", "电源"),
    CASE("Case", "机箱"),
    COOLER("Cooler", "散热器");

    private final String code;
    private final String description;

    HardwareType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}