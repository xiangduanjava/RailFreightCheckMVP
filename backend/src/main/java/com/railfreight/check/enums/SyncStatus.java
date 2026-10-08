package com.railfreight.check.enums;

/** 采集同步状态：MVP 手动补录提交即落库，恒为「已同步」；PENDING 仅为未来硬件/离线场景预留（设计决策 7）。 */
public enum SyncStatus {
    PENDING(0, "待同步"),
    SYNCED(1, "已同步");

    private final int code;
    private final String label;

    SyncStatus(int code, String label) {
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
