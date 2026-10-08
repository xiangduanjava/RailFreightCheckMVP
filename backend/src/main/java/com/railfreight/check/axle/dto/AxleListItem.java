package com.railfreight.check.axle.dto;

/** 轮轴列表项（总览页表格一行）。 */
public record AxleListItem(
        String axleId,
        String model,
        String trainNo,
        String status,
        String latestDetection,
        String repairGrade) {
}
