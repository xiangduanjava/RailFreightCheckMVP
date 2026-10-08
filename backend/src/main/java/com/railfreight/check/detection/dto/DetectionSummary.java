package com.railfreight.check.detection.dto;

/** 数据上传状态汇总：今日采集 / 已上传 / 待同步。 */
public record DetectionSummary(
        long todayCollected,
        long uploaded,
        long pending) {
}
