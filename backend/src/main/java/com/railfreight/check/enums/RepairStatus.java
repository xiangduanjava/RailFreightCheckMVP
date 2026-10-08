package com.railfreight.check.enums;

/** 检修流程状态：扣修后走到哪一步。 */
public enum RepairStatus {
    IN_REPAIR(0, "在修"),
    PENDING_REINSPECT(1, "待复检"),
    PENDING_ACCEPTANCE(2, "待验收"),
    RELEASED(3, "已出库");

    private final int code;
    private final String label;

    RepairStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static RepairStatus fromLabel(String label) {
        for (RepairStatus v : values()) {
            if (v.label.equals(label)) {
                return v;
            }
        }
        return null;
    }
}
