package com.railfreight.check.axle;

import com.railfreight.check.axle.dto.AxleDetail;
import com.railfreight.check.axle.dto.AxleListItem;
import com.railfreight.check.axle.dto.CreateAxleRequest;
import com.railfreight.check.axle.dto.HistoryItem;
import com.railfreight.check.axle.dto.UpdateStatusRequest;
import com.railfreight.check.enums.EventType;
import com.railfreight.check.enums.LifecycleStatus;
import com.railfreight.check.enums.RepairGrade;
import com.railfreight.check.enums.RepairStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class AxleService {

    private static final String FILTER_ALL = "全部";

    private static final String PRODUCED_NOTE = "轮轴制造完成，完成条码创建与基础参数录入。";

    private static final Set<String> AXLE_POS = Set.of("1位轴", "2位轴", "3位轴", "4位轴");
    private static final Set<String> SIDE = Set.of("左侧", "右侧");
    private static final Set<String> MATERIAL = Set.of("50钢", "35CrMo", "EA4T");

    private final AxleRepository axleRepository;
    private final AxleHistoryRepository historyRepository;

    public AxleService(AxleRepository axleRepository, AxleHistoryRepository historyRepository) {
        this.axleRepository = axleRepository;
        this.historyRepository = historyRepository;
    }

    /** 轮轴列表：按状态筛选 + 关键字搜索。 */
    @Transactional(readOnly = true)
    public List<AxleListItem> list(String status, String keyword) {
        Stream<Axle> stream = axleRepository.findAll().stream()
                .sorted(Comparator.comparing(Axle::getAxleId));

        if (status != null && !status.isBlank() && !FILTER_ALL.equals(status)) {
            stream = stream.filter(a -> status.equals(displayStatus(a)));
        }

        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(a -> matchesKeyword(a, k));
        }

        return stream.map(this::toListItem).toList();
    }

    /** 轮轴详情：数字身份证 + 检修参数 + 履历。 */
    @Transactional(readOnly = true)
    public AxleDetail detail(String axleId) {
        Axle axle = findAxle(axleId);
        List<HistoryItem> history = historyRepository.findByAxleIdOrderByEventTimeDesc(axleId)
                .stream()
                .map(h -> new HistoryItem(
                        h.getEventTime(),
                        h.getEventType().getLabel(),
                        h.getNote()))
                .toList();
        return toDetail(axle, history);
    }

    /** 更新状态（生命周期/检修流程），并写入履历。 */
    @Transactional
    public AxleDetail updateStatus(String axleId, UpdateStatusRequest req) {
        Axle axle = findAxle(axleId);

        LifecycleStatus newLifecycle = axle.getLifecycleStatus();
        RepairStatus newRepair = axle.getRepairStatus();

        if (req.lifecycleStatus() != null && !req.lifecycleStatus().isBlank()) {
            newLifecycle = LifecycleStatus.fromLabel(req.lifecycleStatus().trim());
            if (newLifecycle == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的生命周期状态");
            }
        }
        if (req.repairStatus() != null && !req.repairStatus().isBlank()) {
            newRepair = RepairStatus.fromLabel(req.repairStatus().trim());
            if (newRepair == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的检修流程状态");
            }
        }

        boolean lifecycleChanged = newLifecycle != axle.getLifecycleStatus();
        boolean repairChanged = newRepair != axle.getRepairStatus();

        if (lifecycleChanged) {
            axle.setLifecycleStatus(newLifecycle);
            if (newLifecycle == LifecycleStatus.SCRAPPED) {
                appendHistory(axleId, EventType.SCRAPPED, "轮轴标记报废，不再进入生产环节");
            }
        }
        if (repairChanged) {
            axle.setRepairStatus(newRepair);
            if (newRepair == RepairStatus.RELEASED) {
                axle.setLifecycleStatus(LifecycleStatus.IN_SERVICE);
                appendHistory(axleId, EventType.ACCEPTED, "修复验收通过，轮轴出库");
            } else {
                appendHistory(axleId, EventType.REINSPECT, "状态更新为「" + newRepair.getLabel() + "」");
            }
        }

        axleRepository.save(axle);
        return detail(axleId);
    }

    /** 新建轮轴：生成唯一 ID、白名单校验、初始化状态并写「生产出厂登记」履历。 */
    public AxleDetail create(CreateAxleRequest req) {
        Axle axle = toEntity(req, nextAxleId());
        try {
            axleRepository.save(axle);
        } catch (DataIntegrityViolationException e) {
            // 并发撞主键：换下一个序号重试一次（axle_id 主键唯一约束兜底）
            axle.setAxleId(nextAxleId());
            axleRepository.save(axle);
        }
        appendHistory(axle.getAxleId(), EventType.PRODUCED, PRODUCED_NOTE);
        return detail(axle.getAxleId());
    }

    // ---- helpers ----

    private Axle findAxle(String axleId) {
        return axleRepository.findById(axleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "轮轴不存在: " + axleId));
    }

    private void appendHistory(String axleId, EventType type, String note) {
        AxleHistory h = new AxleHistory();
        h.setAxleId(axleId);
        h.setEventType(type);
        h.setEventTime(LocalDateTime.now());
        h.setNote(note);
        historyRepository.save(h);
    }

    /** 请求体 -> 实体：必填字段落库，枚举类字段做白名单校验，初始状态固定。 */
    private Axle toEntity(CreateAxleRequest req, String axleId) {
        Axle a = new Axle();
        a.setAxleId(axleId);
        a.setModel(req.model().trim());
        a.setTrainNo(req.trainNo().trim());
        a.setRepairGrade(parseRepair(req.repair()));
        a.setAxlePos(requireIn(AXLE_POS, "轴位", req.axlePos()));
        a.setFactory(req.factory().trim());
        a.setProduceDate(req.produceDate());
        a.setNominalWheel(req.nominalWheel());
        a.setActualWheel(req.actualWheel());

        a.setSide(optionalIn(SIDE, "左右侧", req.side()));
        a.setBearing(trimToNull(req.bearing()));
        a.setMaterial(optionalIn(MATERIAL, "材质", req.material()));
        a.setSerialNo(trimToNull(req.serialNo()));
        a.setCertNo(trimToNull(req.certNo()));
        a.setInstallDate(req.installDate());
        a.setJournalDia(req.journalDia());
        a.setFlangeThick(req.flangeThick());
        a.setRimThick(req.rimThick());
        a.setBackGauge(req.backGauge());
        a.setRemark(trimToNull(req.remark()));

        a.setLifecycleStatus(LifecycleStatus.IN_SERVICE);
        a.setRepairStatus(null);
        a.setRepairCount(0);
        a.setAlertType(null);
        // wear/crack/vibration/temp/latestDetection/remainingLife 保持 null，由设备采集模块后续覆盖
        return a;
    }

    /** 生成全局唯一 ID：当日前缀 + 当日已存在最大序号 +1，补零到 3 位。 */
    private String nextAxleId() {
        String prefix = "AX-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        int maxSeq = axleRepository.findByAxleIdStartingWith(prefix).stream()
                .map(Axle::getAxleId)
                .map(id -> Integer.parseInt(id.substring(id.length() - 3)))
                .max(Integer::compareTo)
                .orElse(0);
        return prefix + String.format("%03d", maxSeq + 1);
    }

    /** 修程：容忍「例行检查」并归一为「例行」，其余字面值一一对应。 */
    private RepairGrade parseRepair(String label) {
        String normalized = "例行检查".equals(label.trim()) ? "例行" : label.trim();
        RepairGrade grade = RepairGrade.fromLabel(normalized);
        if (grade == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的修程: " + label);
        }
        return grade;
    }

    private String requireIn(Set<String> whitelist, String field, String value) {
        String v = value.trim();
        if (!whitelist.contains(v)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的" + field + ": " + value);
        }
        return v;
    }

    private String optionalIn(Set<String> whitelist, String field, String value) {
        String v = trimToNull(value);
        return v == null ? null : requireIn(whitelist, field, v);
    }

    private String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** 列表展示用的单一状态标签（归一化）。 */
    private String displayStatus(Axle a) {
        if (a.getLifecycleStatus() == LifecycleStatus.SCRAPPED) {
            return "报废";
        }
        if (a.getRepairStatus() != null) {
            return a.getRepairStatus().getLabel();
        }
        if (a.getAlertType() != null) {
            return "预警";
        }
        if (a.getLifecycleStatus() == LifecycleStatus.REPAIR) {
            return "检修";
        }
        return "正常";
    }

    private boolean matchesKeyword(Axle a, String k) {
        String grade = a.getRepairGrade() == null ? "" : a.getRepairGrade().getLabel();
        String haystack = String.join(" ",
                nz(a.getAxleId()), nz(a.getModel()), nz(a.getTrainNo()),
                nz(a.getLatestDetection()), grade).toLowerCase(Locale.ROOT);
        return haystack.contains(k);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private AxleListItem toListItem(Axle a) {
        return new AxleListItem(
                a.getAxleId(),
                a.getModel(),
                a.getTrainNo(),
                displayStatus(a),
                a.getLatestDetection(),
                a.getRepairGrade() == null ? null : a.getRepairGrade().getLabel());
    }

    private AxleDetail toDetail(Axle a, List<HistoryItem> history) {
        return new AxleDetail(
                a.getAxleId(),
                a.getModel(),
                a.getTrainNo(),
                a.getFactory(),
                a.getProduceDate(),
                a.getInstallDate(),
                displayStatus(a),
                a.getLifecycleStatus() == null ? null : a.getLifecycleStatus().getLabel(),
                a.getRepairStatus() == null ? null : a.getRepairStatus().getLabel(),
                a.getRepairCount(),
                a.getRemainingLife(),
                a.getWear(),
                a.getCrack(),
                a.getVibration(),
                a.getTemp(),
                history);
    }
}
