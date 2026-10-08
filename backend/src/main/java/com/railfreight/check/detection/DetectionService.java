package com.railfreight.check.detection;

import com.railfreight.check.axle.Axle;
import com.railfreight.check.axle.AxleHistory;
import com.railfreight.check.axle.AxleHistoryRepository;
import com.railfreight.check.axle.AxleRepository;
import com.railfreight.check.axle.AxleService;
import com.railfreight.check.axle.dto.AxleDetail;
import com.railfreight.check.detection.dto.CreateDetectionRequest;
import com.railfreight.check.detection.dto.DetectionItem;
import com.railfreight.check.detection.dto.DetectionSummary;
import com.railfreight.check.enums.EventType;
import com.railfreight.check.enums.Station;
import com.railfreight.check.enums.SyncStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 工位数据手动补录：校验 + 写检测记录 + 回写轮轴 + 写履历 + 同步记录与汇总查询。 */
@Service
public class DetectionService {

    private final DetectionRecordRepository recordRepository;
    private final AxleRepository axleRepository;
    private final AxleHistoryRepository historyRepository;
    private final AxleService axleService;

    public DetectionService(DetectionRecordRepository recordRepository,
                            AxleRepository axleRepository,
                            AxleHistoryRepository historyRepository,
                            AxleService axleService) {
        this.recordRepository = recordRepository;
        this.axleRepository = axleRepository;
        this.historyRepository = historyRepository;
        this.axleService = axleService;
    }

    /** 手动补录：同一事务内写检测记录、回写轮轴、写检测履历，返回更新后的轮轴详情。 */
    @Transactional
    public AxleDetail create(CreateDetectionRequest req) {
        Axle axle = axleRepository.findById(req.axleId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "轮轴不存在: " + req.axleId()));

        Station station = Station.fromLabel(req.station().trim());
        if (station == null || !station.isCollectable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的工位: " + req.station());
        }

        // 至少填写一项检测指标（覆盖手持终端补录「至少一项」，设计决策 2）
        if (req.wear() == null && req.crack() == null && req.vibration() == null && req.temp() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请至少填写一项检测指标");
        }
        // 工位特定必填检测项
        String missing = station.missingRequiredMetric(req.wear(), req.crack());
        if (missing != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "工位「" + station.getLabel() + "」必填检测项「" + missing + "」缺失");
        }

        LocalDateTime detectionTime = req.detectionTime() == null ? LocalDateTime.now() : req.detectionTime();

        DetectionRecord record = new DetectionRecord();
        record.setAxleId(axle.getAxleId());
        record.setStation(station);
        record.setDetectionTime(detectionTime);
        record.setWear(req.wear());
        record.setCrack(req.crack());
        record.setVibration(req.vibration());
        record.setTemp(req.temp());
        record.setRemark(trimToNull(req.remark()));
        record.setSyncStatus(SyncStatus.SYNCED);
        record.setSyncTime(LocalDateTime.now());
        recordRepository.save(record);

        // 回写轮轴：仅覆盖本次填写的指标 + 最近检测来源（设计决策 4）
        if (req.wear() != null) {
            axle.setWear(req.wear());
        }
        if (req.crack() != null) {
            axle.setCrack(req.crack());
        }
        if (req.vibration() != null) {
            axle.setVibration(req.vibration());
        }
        if (req.temp() != null) {
            axle.setTemp(req.temp());
        }
        axle.setLatestDetection(shortLabel(station));
        axleRepository.save(axle);

        appendHistory(axle.getAxleId(), station);

        return axleService.detail(axle.getAxleId());
    }

    /** 最近同步记录（时间倒序）。 */
    @Transactional(readOnly = true)
    public List<DetectionItem> list(int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        return recordRepository.findAllByOrderByDetectionTimeDescIdDesc(PageRequest.of(0, size))
                .stream()
                .map(r -> new DetectionItem(
                        r.getStation().getLabel(),
                        r.getAxleId(),
                        r.getDetectionTime(),
                        r.getSyncStatus().getLabel()))
                .toList();
    }

    /** 上传状态汇总：今日采集 / 已上传 / 待同步（均以今日为口径，已上传 + 待同步 = 今日采集）。 */
    @Transactional(readOnly = true)
    public DetectionSummary summary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCollected = recordRepository.countByDetectionTimeAfter(todayStart);
        long uploaded = recordRepository.countByDetectionTimeAfterAndSyncStatus(todayStart, SyncStatus.SYNCED);
        long pending = recordRepository.countByDetectionTimeAfterAndSyncStatus(todayStart, SyncStatus.PENDING);
        return new DetectionSummary(todayCollected, uploaded, pending);
    }

    // ---- helpers ----

    /** 最近检测来源短标签，对齐 DataSeeder 既有 latestDetection 取值风格（设计决策 4）。 */
    private String shortLabel(Station station) {
        return switch (station) {
            case WHEEL_DETECTOR -> "轮对尺寸检测";
            case ULTRASONIC -> "超声波探伤";
            case HANDHELD -> "手持补录";
            default -> station.getLabel();
        };
    }

    private void appendHistory(String axleId, Station station) {
        AxleHistory h = new AxleHistory();
        h.setAxleId(axleId);
        h.setEventType(EventType.DETECTED);
        h.setEventTime(LocalDateTime.now());
        h.setNote("「" + station.getLabel() + "」检测数据已上传");
        historyRepository.save(h);
    }

    private String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
