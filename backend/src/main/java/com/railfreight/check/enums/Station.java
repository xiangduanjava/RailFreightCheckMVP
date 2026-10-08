package com.railfreight.check.enums;

import java.math.BigDecimal;

/** 工位枚举：取值对齐 prototype/device.html 的四台设备，携带「必填检测项」判定逻辑（设计决策 2）。 */
public enum Station {
    WHEEL_DETECTOR(0, "轮对尺寸检测机"),
    ULTRASONIC(1, "超声波探伤仪"),
    WEIGHING(2, "自动称重系统"),
    HANDHELD(3, "手持终端补录");

    private final int code;
    private final String label;

    Station(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static Station fromLabel(String label) {
        for (Station v : values()) {
            if (v.label.equals(label)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 该工位「特定必填检测项」缺失时返回该项标签，否则返回 null。
     * 手持终端补录的「至少一项」由服务层统一校验；自动称重系统无检测项（且不作为补录工位）。
     */
    public String missingRequiredMetric(BigDecimal wear, BigDecimal crack) {
        return switch (this) {
            case WHEEL_DETECTOR -> wear == null ? "轮径磨损" : null;
            case ULTRASONIC -> crack == null ? "裂纹深度" : null;
            default -> null;
        };
    }

    /** 是否可作为手动补录工位：自动称重系统仅作静态展示，不作为补录目标（设计决策 2）。 */
    public boolean isCollectable() {
        return this != WEIGHING;
    }
}
