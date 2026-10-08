package com.railfreight.check.enums;

/** 预警类型（派生标志，叠加于状态之上）。 */
public enum AlertType {
    PARAM_EXCEED(0, "参数超标"),
    OVERDUE_REPAIR(1, "超期未修"),
    NEAR_SCRAP(2, "临近报废");

    private final int code;
    private final String label;

    AlertType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
