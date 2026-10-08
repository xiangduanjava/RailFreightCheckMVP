package com.railfreight.check.enums;

/** 生命周期状态：轮轴在不在用。 */
public enum LifecycleStatus {
    IN_SERVICE(0, "在役"),
    REPAIR(1, "检修"),
    SCRAPPED(2, "报废");

    private final int code;
    private final String label;

    LifecycleStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static LifecycleStatus fromLabel(String label) {
        for (LifecycleStatus v : values()) {
            if (v.label.equals(label)) {
                return v;
            }
        }
        return null;
    }
}
