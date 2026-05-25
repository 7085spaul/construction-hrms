package com.payrollapplication.payroll.model;

import org.hibernate.annotations.Check;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "workers", indexes = {
        @Index(name = "idx_workers_phone", columnList = "phone"),
        @Index(name = "idx_workers_designation", columnList = "designation")
})
@Check(constraints = "daily_wage_rate >= 0")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkerDesignation designation;

    @Column(name = "daily_wage_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyWageRate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    public Worker() {
    }

    public Worker(String name, String phone, WorkerDesignation designation, BigDecimal dailyWageRate, ActiveStatus status) {
        this.name = name;
        this.phone = phone;
        this.designation = designation;
        this.dailyWageRate = dailyWageRate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public WorkerDesignation getDesignation() {
        return designation;
    }

    public void setDesignation(WorkerDesignation designation) {
        this.designation = designation;
    }

    public BigDecimal getDailyWageRate() {
        return dailyWageRate;
    }

    public void setDailyWageRate(BigDecimal dailyWageRate) {
        this.dailyWageRate = dailyWageRate;
    }

    public ActiveStatus getStatus() {
        return status;
    }

    public void setStatus(ActiveStatus status) {
        this.status = status;
    }
}
