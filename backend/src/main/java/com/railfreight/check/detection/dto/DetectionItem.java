package com.railfreight.check.detection.dto;

import java.time.LocalDateTime;

/** 采集同步记录项（列表展示）。 */
public record DetectionItem(
        String station,
        String axleId,
        LocalDateTime detectionTime,
        String syncStatus) {
}
