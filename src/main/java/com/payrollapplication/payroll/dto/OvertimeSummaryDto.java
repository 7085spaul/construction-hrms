package com.payrollapplication.payroll.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OvertimeSummaryDto {

    private Long workerId;
    private String workerName;
    private String month;
    private BigDecimal totalOvertimeHours = BigDecimal.ZERO;
    private BigDecimal totalPayoutAmount = BigDecimal.ZERO;
    private String settlementStatus;
    private List<OvertimeBreakdownDto> breakdown = new ArrayList<>();

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public BigDecimal getTotalPayoutAmount() {
        return totalPayoutAmount;
    }

    public void setTotalPayoutAmount(BigDecimal totalPayoutAmount) {
        this.totalPayoutAmount = totalPayoutAmount;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public List<OvertimeBreakdownDto> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(List<OvertimeBreakdownDto> breakdown) {
        this.breakdown = breakdown;
    }

    public static class OvertimeBreakdownDto {
        private String date;
        private BigDecimal overtimeHours;
        private BigDecimal overtimeRate;
        private BigDecimal amount;
        private String settlementStatus;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
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

        public String getSettlementStatus() {
            return settlementStatus;
        }

        public void setSettlementStatus(String settlementStatus) {
            this.settlementStatus = settlementStatus;
        }
    }
}
