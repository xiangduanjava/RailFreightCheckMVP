package com.railfreight.check.axle.dto;

import java.time.LocalDateTime;

/** 履历事件项。 */
public record HistoryItem(
        LocalDateTime eventTime,
        String eventType,
        String note) {
}
