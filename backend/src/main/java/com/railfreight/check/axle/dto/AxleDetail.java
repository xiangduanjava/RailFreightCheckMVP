package com.railfreight.check.axle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 轮轴详情（数字身份证 + 检修参数 + 履历）。 */
public record AxleDetail(
        String axleId,
        String model,
        String trainNo,
        String factory,
        LocalDate produceDate,
        LocalDate installDate,
        String status,
        String lifecycleStatus,
        String repairStatus,
        Integer repairCount,
        Integer remainingLife,
        BigDecimal wear,
        BigDecimal crack,
        BigDecimal vibration,
        BigDecimal temp,
        List<HistoryItem> history) {
}
