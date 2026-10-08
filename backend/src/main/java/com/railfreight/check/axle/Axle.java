package com.railfreight.check.axle;

import com.railfreight.check.enums.AlertType;
import com.railfreight.check.enums.LifecycleStatus;
import com.railfreight.check.enums.RepairGrade;
import com.railfreight.check.enums.RepairStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 轮轴基础信息表。 */
@Entity
@Table(name = "axle_info")
public class Axle {

    @Id
    @Column(name = "axle_id", length = 32)
    private String axleId;

    @Column(name = "model", length = 32)
    private String model;

    @Column(name = "train_no", length = 32)
    private String trainNo;

    @Column(name = "factory", length = 64)
    private String factory;

    @Column(name = "produce_date")
    private LocalDate produceDate;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "lifecycle_status")
    private LifecycleStatus lifecycleStatus;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "repair_status")
    private RepairStatus repairStatus;

    @Column(name = "repair_count")
    private Integer repairCount;

    @Column(name = "remaining_life")
    private Integer remainingLife;

    @Column(name = "wear", precision = 5, scale = 2)
    private BigDecimal wear;

    @Column(name = "crack", precision = 5, scale = 2)
    private BigDecimal crack;

    @Column(name = "vibration", precision = 5, scale = 2)
    private BigDecimal vibration;

    @Column(name = "temp", precision = 5, scale = 2)
    private BigDecimal temp;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "repair_grade")
    private RepairGrade repairGrade;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "alert_type")
    private AlertType alertType;

    @Column(name = "latest_detection", length = 64)
    private String latestDetection;

    @Column(name = "axle_pos", length = 16)
    private String axlePos;

    @Column(name = "side", length = 8)
    private String side;

    @Column(name = "bearing", length = 32)
    private String bearing;

    @Column(name = "material", length = 32)
    private String material;

    @Column(name = "serial_no", length = 64)
    private String serialNo;

    @Column(name = "cert_no", length = 64)
    private String certNo;

    @Column(name = "nominal_wheel", precision = 6, scale = 2)
    private BigDecimal nominalWheel;

    @Column(name = "actual_wheel", precision = 6, scale = 2)
    private BigDecimal actualWheel;

    @Column(name = "journal_dia", precision = 6, scale = 2)
    private BigDecimal journalDia;

    @Column(name = "flange_thick", precision = 6, scale = 2)
    private BigDecimal flangeThick;

    @Column(name = "rim_thick", precision = 6, scale = 2)
    private BigDecimal rimThick;

    @Column(name = "back_gauge", precision = 6, scale = 2)
    private BigDecimal backGauge;

    @Column(name = "remark", length = 512)
    private String remark;

    // ---- getters / setters ----

    public String getAxleId() {
        return axleId;
    }

    public void setAxleId(String axleId) {
        this.axleId = axleId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public LocalDate getProduceDate() {
        return produceDate;
    }

    public void setProduceDate(LocalDate produceDate) {
        this.produceDate = produceDate;
    }

    public LocalDate getInstallDate() {
        return installDate;
    }

    public void setInstallDate(LocalDate installDate) {
        this.installDate = installDate;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public RepairStatus getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(RepairStatus repairStatus) {
        this.repairStatus = repairStatus;
    }

    public Integer getRepairCount() {
        return repairCount;
    }

    public void setRepairCount(Integer repairCount) {
        this.repairCount = repairCount;
    }

    public Integer getRemainingLife() {
        return remainingLife;
    }

    public void setRemainingLife(Integer remainingLife) {
        this.remainingLife = remainingLife;
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

    public RepairGrade getRepairGrade() {
        return repairGrade;
    }

    public void setRepairGrade(RepairGrade repairGrade) {
        this.repairGrade = repairGrade;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getLatestDetection() {
        return latestDetection;
    }

    public void setLatestDetection(String latestDetection) {
        this.latestDetection = latestDetection;
    }

    public String getAxlePos() {
        return axlePos;
    }

    public void setAxlePos(String axlePos) {
        this.axlePos = axlePos;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public BigDecimal getNominalWheel() {
        return nominalWheel;
    }

    public void setNominalWheel(BigDecimal nominalWheel) {
        this.nominalWheel = nominalWheel;
    }

    public BigDecimal getActualWheel() {
        return actualWheel;
    }

    public void setActualWheel(BigDecimal actualWheel) {
        this.actualWheel = actualWheel;
    }

    public BigDecimal getJournalDia() {
        return journalDia;
    }

    public void setJournalDia(BigDecimal journalDia) {
        this.journalDia = journalDia;
    }

    public BigDecimal getFlangeThick() {
        return flangeThick;
    }

    public void setFlangeThick(BigDecimal flangeThick) {
        this.flangeThick = flangeThick;
    }

    public BigDecimal getRimThick() {
        return rimThick;
    }

    public void setRimThick(BigDecimal rimThick) {
        this.rimThick = rimThick;
    }

    public BigDecimal getBackGauge() {
        return backGauge;
    }

    public void setBackGauge(BigDecimal backGauge) {
        this.backGauge = backGauge;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
