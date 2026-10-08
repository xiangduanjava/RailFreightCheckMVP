package com.railfreight.check.enums;

/** 修程（检修等级）。 */
public enum RepairGrade {
    ROUTINE(0, "例行"),
    MIDDLE(1, "中修"),
    MAJOR(2, "大修"),
    REINSPECT(3, "复检");

    private final int code;
    private final String label;

    RepairGrade(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static RepairGrade fromLabel(String label) {
        for (RepairGrade v : values()) {
            if (v.label.equals(label)) {
                return v;
            }
        }
        return null;
    }
}
