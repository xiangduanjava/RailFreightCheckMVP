package com.railfreight.check.detection.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 手动补录请求。station 为中文显示值，按工位必填检测项在服务层校验（设计决策 5）。 */
public record CreateDetectionRequest(
        @NotBlank(message = "轮轴编号不能为空") String axleId,
        @NotBlank(message = "工位不能为空") String station,
        LocalDateTime detectionTime,
        BigDecimal wear,
        BigDecimal crack,
        BigDecimal vibration,
        BigDecimal temp,
        String remark) {
}
