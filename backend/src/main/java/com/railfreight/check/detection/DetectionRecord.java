package com.railfreight.check.detection;

import com.railfreight.check.enums.Station;
import com.railfreight.check.enums.SyncStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 检测记录表：每次手动补录写一条，与「轮轴最新值快照」解耦，支撑日志倒序与汇总（设计决策 1）。 */
@Entity
@Table(name = "detection_record")
public class DetectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "axle_id", length = 32, nullable = false)
    private String axleId;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "station", nullable = false)
    private Station station;

    @Column(name = "detection_time", nullable = false)
    private LocalDateTime detectionTime;

    @Column(name = "wear", precision = 5, scale = 2)
    private BigDecimal wear;

    @Column(name = "crack", precision = 5, scale = 2)
    private BigDecimal crack;

    @Column(name = "vibration", precision = 5, scale = 2)
    private BigDecimal vibration;

    @Column(name = "temp", precision = 5, scale = 2)
    private BigDecimal temp;

    @Column(name = "remark", length = 255)
    private String remark;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus;

    @Column(name = "sync_time", nullable = false)
    private LocalDateTime syncTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAxleId() {
        return axleId;
    }

    public void setAxleId(String axleId) {
        this.axleId = axleId;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public LocalDateTime getDetectionTime() {
        return detectionTime;
    }

    public void setDetectionTime(LocalDateTime detectionTime) {
        this.detectionTime = detectionTime;
    }

    public BigDecimal getWear() {
        return wear;
    }

    public void setWear(BigDecimal wear) {
        this.wear = wear;
    }

    public BigDecimal getCrack() {
        return crack;
    }

    public void setCrack(BigDecimal crack) {
        this.crack = crack;
    }

    public BigDecimal getVibration() {
        return vibration;
    }

    public void setVibration(BigDecimal vibration) {
        this.vibration = vibration;
    }

    public BigDecimal getTemp() {
        return temp;
    }

    public void setTemp(BigDecimal temp) {
        this.temp = temp;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public LocalDateTime getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(LocalDateTime syncTime) {
        this.syncTime = syncTime;
    }
}
