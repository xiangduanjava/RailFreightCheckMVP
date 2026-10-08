package com.railfreight.check.axle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 新建轮轴请求。必填字段用 Bean Validation 在 Controller 层拦截，枚举类字段在服务层做白名单校验。 */
public record CreateAxleRequest(
        @NotBlank(message = "车型不能为空") String model,
        @NotBlank(message = "车号不能为空") String trainNo,
        @NotBlank(message = "修程不能为空") String repair,
        @NotBlank(message = "轴位不能为空") String axlePos,
        @NotBlank(message = "生产厂家不能为空") String factory,
        @NotNull(message = "生产日期不能为空") LocalDate produceDate,
        @NotNull(message = "公称轮径不能为空") BigDecimal nominalWheel,
        @NotNull(message = "实测轮径不能为空") BigDecimal actualWheel,
        String side,
        String bearing,
        String material,
        String serialNo,
        String certNo,
        LocalDate installDate,
        BigDecimal journalDia,
        BigDecimal flangeThick,
        BigDecimal rimThick,
        BigDecimal backGauge,
        String remark) {
}
