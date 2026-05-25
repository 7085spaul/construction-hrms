package com.payrollapplication.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceRecordDto {

    private Long timeEntryId;
    private Long employeeId;
    private String employeeName;
    private Long siteId;
    private String siteName;
    private Long shiftTypeId;
    private String shiftName;
    private LocalDate workDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Integer breakMinutes;
    private BigDecimal regularHours;
    private BigDecimal overtimeHours;
    private BigDecimal doubleTimeHours;
    private Boolean onSite;
    private Boolean approved;

    public Long getTimeEntryId() { return timeEntryId; }
    public void setTimeEntryId(Long timeEntryId) { this.timeEntryId = timeEntryId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getSiteId() { return siteId; }
    public void setSiteId(Long siteId) { this.siteId = siteId; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public Long getShiftTypeId() { return shiftTypeId; }
    public void setShiftTypeId(Long shiftTypeId) { this.shiftTypeId = shiftTypeId; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }

    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }

    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }

    public BigDecimal getRegularHours() { return regularHours; }
    public void setRegularHours(BigDecimal regularHours) { this.regularHours = regularHours; }

    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

    public BigDecimal getDoubleTimeHours() { return doubleTimeHours; }
    public void setDoubleTimeHours(BigDecimal doubleTimeHours) { this.doubleTimeHours = doubleTimeHours; }

    public Boolean getOnSite() { return onSite; }
    public void setOnSite(Boolean onSite) { this.onSite = onSite; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
}
