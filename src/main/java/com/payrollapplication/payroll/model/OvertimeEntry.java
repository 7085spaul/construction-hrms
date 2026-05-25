package com.payrollapplication.payroll.model;

import org.hibernate.annotations.Check;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "overtime_entries", indexes = {
        @Index(name = "idx_overtime_entries_worker", columnList = "worker_id"),
        @Index(name = "idx_overtime_entries_attendance", columnList = "attendance_log_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_overtime_entry_attendance_date", columnNames = {"attendance_log_id", "entry_date"})
})
@Check(constraints = "overtime_hours >= 0 AND overtime_rate >= 0 AND amount >= 0")
public class OvertimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attendance_log_id", nullable = false)
    private AttendanceLog attendanceLog;

    @Column(name = "entry_date", nullable = false)
    private LocalDate date;

    @Column(name = "overtime_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal overtimeRate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    public OvertimeEntry() {
    }

    public OvertimeEntry(Worker worker, AttendanceLog attendanceLog, LocalDate date, BigDecimal overtimeHours, BigDecimal overtimeRate, BigDecimal amount, SettlementStatus settlementStatus) {
        this.worker = worker;
        this.attendanceLog = attendanceLog;
        this.date = date;
        this.overtimeHours = overtimeHours;
        this.overtimeRate = overtimeRate;
        this.amount = amount;
        this.settlementStatus = settlementStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public AttendanceLog getAttendanceLog() {
        return attendanceLog;
    }

    public void setAttendanceLog(AttendanceLog attendanceLog) {
        this.attendanceLog = attendanceLog;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getOvertimeRate() {
        return overtimeRate;
    }

    public void setOvertimeRate(BigDecimal overtimeRate) {
        this.overtimeRate = overtimeRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public SettlementStatus getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(SettlementStatus settlementStatus) {
        this.settlementStatus = settlementStatus;
    }
}
