package com.payrollapplication.payroll.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MonthlySettlementReportDto {

    private String period;
    private Long siteId;
    private String siteName;
    private int totalWorkers;
    private BigDecimal totalRegularHours = BigDecimal.ZERO;
    private BigDecimal totalOvertimeHours = BigDecimal.ZERO;
    private BigDecimal totalDoubleTimeHours = BigDecimal.ZERO;
    private BigDecimal totalRegularPay = BigDecimal.ZERO;
    private BigDecimal totalOvertimePay = BigDecimal.ZERO;
    private BigDecimal totalDoubleTimePay = BigDecimal.ZERO;
    private BigDecimal grandTotalPay = BigDecimal.ZERO;
    private List<WorkerSettlementLineDto> workers = new ArrayList<>();

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }

    public BigDecimal getTotalRegularHours() { return totalRegularHours; }
    public void setTotalRegularHours(BigDecimal totalRegularHours) { this.totalRegularHours = totalRegularHours; }

    public BigDecimal getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }

    public BigDecimal getTotalDoubleTimeHours() { return totalDoubleTimeHours; }
    public void setTotalDoubleTimeHours(BigDecimal totalDoubleTimeHours) { this.totalDoubleTimeHours = totalDoubleTimeHours; }

    public BigDecimal getTotalRegularPay() { return totalRegularPay; }
    public void setTotalRegularPay(BigDecimal totalRegularPay) { this.totalRegularPay = totalRegularPay; }

    public BigDecimal getTotalOvertimePay() { return totalOvertimePay; }
    public void setTotalOvertimePay(BigDecimal totalOvertimePay) { this.totalOvertimePay = totalOvertimePay; }

    public BigDecimal getTotalDoubleTimePay() { return totalDoubleTimePay; }
    public void setTotalDoubleTimePay(BigDecimal totalDoubleTimePay) { this.totalDoubleTimePay = totalDoubleTimePay; }

    public BigDecimal getGrandTotalPay() { return grandTotalPay; }
    public void setGrandTotalPay(BigDecimal grandTotalPay) { this.grandTotalPay = grandTotalPay; }

    public List<WorkerSettlementLineDto> getWorkers() { return workers; }
    public void setWorkers(List<WorkerSettlementLineDto> workers) { this.workers = workers; }

    public static class WorkerSettlementLineDto {
        private Long employeeId;
        private String employeeName;
        private int daysWorked;
        private BigDecimal regularHours = BigDecimal.ZERO;
        private BigDecimal overtimeHours = BigDecimal.ZERO;
        private BigDecimal doubleTimeHours = BigDecimal.ZERO;
        private BigDecimal regularPay = BigDecimal.ZERO;
        private BigDecimal overtimePay = BigDecimal.ZERO;
        private BigDecimal doubleTimePay = BigDecimal.ZERO;
        private BigDecimal totalPay = BigDecimal.ZERO;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public int getDaysWorked() { return daysWorked; }
        public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }

        public BigDecimal getRegularHours() { return regularHours; }
        public void setRegularHours(BigDecimal regularHours) { this.regularHours = regularHours; }

        public BigDecimal getOvertimeHours() { return overtimeHours; }
        public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

        public BigDecimal getDoubleTimeHours() { return doubleTimeHours; }
        public void setDoubleTimeHours(BigDecimal doubleTimeHours) { this.doubleTimeHours = doubleTimeHours; }

        public BigDecimal getRegularPay() { return regularPay; }
        public void setRegularPay(BigDecimal regularPay) { this.regularPay = regularPay; }

        public BigDecimal getOvertimePay() { return overtimePay; }
        public void setOvertimePay(BigDecimal overtimePay) { this.overtimePay = overtimePay; }

        public BigDecimal getDoubleTimePay() { return doubleTimePay; }
        public void setDoubleTimePay(BigDecimal doubleTimePay) { this.doubleTimePay = doubleTimePay; }

        public BigDecimal getTotalPay() { return totalPay; }
        public void setTotalPay(BigDecimal totalPay) { this.totalPay = totalPay; }
    }
}
