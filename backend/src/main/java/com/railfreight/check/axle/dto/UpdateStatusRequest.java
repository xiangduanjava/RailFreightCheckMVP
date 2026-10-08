package com.railfreight.check.axle.dto;

/** 状态更新请求，字段值为中文显示值，可部分更新。 */
public record UpdateStatusRequest(
        String lifecycleStatus,
        String repairStatus) {
}
