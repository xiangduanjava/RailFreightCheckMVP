package com.railfreight.check.detection;

import com.railfreight.check.enums.SyncStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DetectionRecordRepository extends JpaRepository<DetectionRecord, Long> {

    /** 按检测时间倒序（同刻按 id 倒序保证稳定），配合 Pageable 取最近 N 条。 */
    List<DetectionRecord> findAllByOrderByDetectionTimeDescIdDesc(Pageable pageable);

    long countByDetectionTimeAfter(LocalDateTime start);

    long countByDetectionTimeAfterAndSyncStatus(LocalDateTime start, SyncStatus syncStatus);
}
