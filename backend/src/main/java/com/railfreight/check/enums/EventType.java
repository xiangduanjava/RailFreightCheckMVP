package com.railfreight.check.enums;

/** 履历事件类型。 */
public enum EventType {
    PRODUCED(0, "生产出厂登记"),
    INSTALLED(1, "装车上线"),
    REINSPECT(2, "复检处理"),
    ACCEPTED(3, "修复验收"),
    SCRAPPED(4, "报废"),
    DETECTED(5, "检测记录");

    private final int code;
    private final String label;

    EventType(int code, String label) {
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
