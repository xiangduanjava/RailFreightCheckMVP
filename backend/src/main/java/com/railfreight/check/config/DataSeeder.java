package com.railfreight.check.config;

import com.railfreight.check.axle.Axle;
import com.railfreight.check.axle.AxleHistory;
import com.railfreight.check.axle.AxleHistoryRepository;
import com.railfreight.check.axle.AxleRepository;
import com.railfreight.check.enums.AlertType;
import com.railfreight.check.enums.EventType;
import com.railfreight.check.enums.LifecycleStatus;
import com.railfreight.check.enums.RepairGrade;
import com.railfreight.check.enums.RepairStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 初始化 mock 轮轴数据（对齐 prototype 样本）。 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AxleRepository axleRepository;
    private final AxleHistoryRepository historyRepository;

    public DataSeeder(AxleRepository axleRepository, AxleHistoryRepository historyRepository) {
        this.axleRepository = axleRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void run(String... args) {
        if (axleRepository.count() > 0) {
            return;
        }
        seedAxle(axle("AX-20240618-104", "CW-200", "K65", "XX铁路轴承厂", "2024-06-18", "2024-08-22",
                        LifecycleStatus.REPAIR, RepairStatus.IN_REPAIR, 3, 2100,
                        "2.8", "1.2", "3.2", "41", RepairGrade.MIDDLE, null, "轮对尺寸检测"),
                hist("2024-06-18 09:00", EventType.PRODUCED, "轮轴制造完成，完成条码创建与基础参数录入。"),
                hist("2024-08-22 10:00", EventType.INSTALLED, "装机到 K65 列车，初始状态正常。"),
                hist("2026-10-04 09:30", EventType.REINSPECT, "超声波探伤发现裂纹风险，已转入中修检修流程。"));

        seedAxle(axle("AX-20240618-112", "CW-200", "K66", "XX铁路轴承厂", "2024-06-18", "2024-08-22",
                        LifecycleStatus.REPAIR, RepairStatus.PENDING_REINSPECT, 2, 1860,
                        "2.3", "1.0", "2.8", "38", RepairGrade.MIDDLE, null, "超声波探伤"),
                hist("2024-06-18 09:00", EventType.PRODUCED, "轮轴制造完成，完成条码创建与基础参数录入。"),
                hist("2024-08-22 10:00", EventType.INSTALLED, "装机到 K66 列车，初始状态正常。"),
                hist("2026-10-04 11:00", EventType.REINSPECT, "超声波探伤待复检确认。"));

        seedAxle(axle("AX-20240618-077", "CW-120", "T31", "XX铁路轴承厂", "2024-06-18", "2024-08-10",
                        LifecycleStatus.IN_SERVICE, null, 1, 3200,
                        "1.5", "0.3", "2.1", "33", RepairGrade.ROUTINE, null, "参数复核"),
                hist("2024-06-18 09:00", EventType.PRODUCED, "轮轴制造完成。"),
                hist("2024-08-10 10:00", EventType.INSTALLED, "装机到 T31 列车，初始状态正常。"));

        seedAxle(axle("AX-20240618-003", "CW-200", "K41", "XX铁路轴承厂", "2023-05-12", "2023-07-20",
                        LifecycleStatus.IN_SERVICE, null, 4, 300,
                        "4.6", "2.0", "4.1", "52", null, AlertType.PARAM_EXCEED, "磨损超标"),
                hist("2023-05-12 09:00", EventType.PRODUCED, "轮轴制造完成。"),
                hist("2023-07-20 10:00", EventType.INSTALLED, "装机到 K41 列车，初始状态正常。"),
                hist("2026-10-04 14:00", EventType.REINSPECT, "磨损超出标准，转预警处理。"));

        seedAxle(axle("AX-20240618-221", "CW-160", "G82", "XX铁路轴承厂", "2024-06-18", "2024-08-15",
                        LifecycleStatus.IN_SERVICE, RepairStatus.RELEASED, 2, 2500,
                        "1.2", "0.4", "1.9", "30", RepairGrade.MIDDLE, null, "交付签字"),
                hist("2024-06-18 09:00", EventType.PRODUCED, "轮轴制造完成。"),
                hist("2024-08-15 10:00", EventType.INSTALLED, "装机到 G82 列车，初始状态正常。"),
                hist("2026-10-04 16:00", EventType.ACCEPTED, "修复完成，交付签字，轮轴已出库。"));
    }

    private void seedAxle(Axle axle, AxleHistory... histories) {
        axleRepository.save(axle);
        for (AxleHistory h : histories) {
            h.setAxleId(axle.getAxleId());
            historyRepository.save(h);
        }
    }

    private AxleHistory hist(String dateTime, EventType type, String note) {
        AxleHistory h = new AxleHistory();
        h.setEventType(type);
        h.setEventTime(LocalDateTime.parse(dateTime.replace(" ", "T")));
        h.setNote(note);
        return h;
    }

    private Axle axle(String id, String model, String trainNo, String factory,
                      String produceDate, String installDate,
                      LifecycleStatus lifecycle, RepairStatus repair,
                      int repairCount, int remainingLife,
                      String wear, String crack, String vibration, String temp,
                      RepairGrade grade, AlertType alert, String latest) {
        Axle a = new Axle();
        a.setAxleId(id);
        a.setModel(model);
        a.setTrainNo(trainNo);
        a.setFactory(factory);
        a.setProduceDate(LocalDate.parse(produceDate));
        a.setInstallDate(installDate == null ? null : LocalDate.parse(installDate));
        a.setLifecycleStatus(lifecycle);
        a.setRepairStatus(repair);
        a.setRepairCount(repairCount);
        a.setRemainingLife(remainingLife);
        a.setWear(new BigDecimal(wear));
        a.setCrack(new BigDecimal(crack));
        a.setVibration(new BigDecimal(vibration));
        a.setTemp(new BigDecimal(temp));
        a.setRepairGrade(grade);
        a.setAlertType(alert);
        a.setLatestDetection(latest);
        return a;
    }
}
