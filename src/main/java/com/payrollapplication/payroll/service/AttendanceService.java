package com.payrollapplication.payroll.service;

import com.payrollapplication.payroll.dto.AttendanceRecordDto;
import com.payrollapplication.payroll.dto.MonthlySettlementReportDto;
import com.payrollapplication.payroll.dto.PaginatedResponseDto;
import com.payrollapplication.payroll.exception.*;
import com.payrollapplication.payroll.model.*;
import com.payrollapplication.payroll.repository.AttendanceLogRepository;
import com.payrollapplication.payroll.repository.ConstructionSiteRepository;
import com.payrollapplication.payroll.repository.EmployeeRepository;
import com.payrollapplication.payroll.repository.OvertimeEntryRepository;
import com.payrollapplication.payroll.repository.ShiftTypeRepository;
import com.payrollapplication.payroll.repository.SiteRepository;
import com.payrollapplication.payroll.repository.TimeEntryRepository;
import com.payrollapplication.payroll.repository.WorkScheduleRepository;
import com.payrollapplication.payroll.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ConstructionSiteRepository constructionSiteRepository;

    @Autowired
    private ShiftTypeRepository shiftTypeRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Autowired
    private OvertimeEntryRepository overtimeEntryRepository;

    @Autowired
    private ActiveWorkersCacheService activeWorkersCacheService;

    @Transactional
    @CacheEvict(value = {"onSiteWorkers", "siteAttendance", "employeeAttendance"}, allEntries = true)
    public AttendanceRecordDto clockIn(Long employeeId, Long siteId, Long shiftTypeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("Only active employees can clock in");
        }

        timeEntryRepository.findOpenEntryByEmployeeId(employeeId).ifPresent(entry -> {
            throw new IllegalStateException("Employee already clocked in at site "
                    + entry.getWorkSchedule().getSite().getName());
        });

        ConstructionSite site = constructionSiteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found: " + siteId));
        if (site.getStatus() != SiteStatus.ACTIVE) {
            throw new IllegalStateException("Site is not active");
        }

        ShiftType shiftType = resolveShiftType(employee, shiftTypeId);
        LocalDate today = LocalDate.now();

        WorkSchedule schedule = workScheduleRepository
                .findByEmployeeIdAndSiteIdAndDate(employeeId, siteId, today)
                .orElseGet(() -> {
                    WorkSchedule ws = new WorkSchedule(employee, site, shiftType, today, 8);
                    ws.setStatus(ScheduleStatus.IN_PROGRESS);
                    return workScheduleRepository.save(ws);
                });

        if (schedule.getStatus() == ScheduleStatus.SCHEDULED) {
            schedule.setStatus(ScheduleStatus.IN_PROGRESS);
            workScheduleRepository.save(schedule);
        }

        TimeEntry entry = new TimeEntry(schedule, LocalDateTime.now());
        entry = timeEntryRepository.save(entry);
        return toDto(entry);
    }

    @Transactional
    @CacheEvict(value = {"onSiteWorkers", "siteAttendance", "employeeAttendance"}, allEntries = true)
    public AttendanceRecordDto clockOut(Long employeeId, Integer breakMinutes) {
        TimeEntry entry = timeEntryRepository.findOpenEntryByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalStateException("No open clock-in for employee: " + employeeId));

        LocalDateTime clockOutTime = LocalDateTime.now();
        long totalMinutes = java.time.Duration.between(entry.getClockIn(), clockOutTime).toMinutes();

        if (breakMinutes != null) {
            if (breakMinutes < 0) {
                throw new IllegalArgumentException("Break minutes cannot be negative");
            }
            if (totalMinutes > 0 && breakMinutes >= totalMinutes) {
                throw new IllegalArgumentException(
                        "Break minutes (" + breakMinutes + ") must be less than time on site (" + totalMinutes + " min). "
                                + "Wait longer before clock-out or use a smaller break.");
            }
            entry.setBreakMinutes(breakMinutes);
        }

        entry.setClockOut(clockOutTime);
        WorkSchedule schedule = entry.getWorkSchedule();
        entry.calculateHours(schedule.getScheduledHours());

        int actualHours = entry.getRegularHours()
                .add(entry.getOvertimeHours())
                .add(entry.getDoubleTimeHours())
                .setScale(0, RoundingMode.UP)
                .intValue();
        schedule.setActualHours(actualHours);
        schedule.setStatus(ScheduleStatus.COMPLETED);
        workScheduleRepository.save(schedule);

        entry = timeEntryRepository.save(entry);
        return toDto(entry);
    }

    @Cacheable(value = "onSiteWorkers", key = "#siteId")
    public List<AttendanceRecordDto> getWorkersOnSite(Long siteId) {
        verifySiteExists(siteId);
        return timeEntryRepository.findOnSiteBySiteId(siteId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "siteAttendance", key = "#siteId + '-' + #date")
    public List<AttendanceRecordDto> getSiteAttendanceForDate(Long siteId, LocalDate date) {
        verifySiteExists(siteId);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return timeEntryRepository.findBySiteIdAndClockInBetween(siteId, start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "employeeAttendance", key = "#employeeId + '-' + #from + '-' + #to")
    public List<AttendanceRecordDto> getEmployeeAttendance(Long employeeId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be on or before to date");
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return timeEntryRepository.findByEmployeeIdAndClockInBetween(employeeId, start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"onSiteWorkers", "siteAttendance", "employeeAttendance"}, allEntries = true)
    public AttendanceRecordDto approveTimeEntry(Long timeEntryId) {
        TimeEntry entry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found: " + timeEntryId));
        if (entry.getClockOut() == null) {
            throw new IllegalStateException("Cannot approve entry without clock-out");
        }
        entry.setIsApproved(true);
        return toDto(timeEntryRepository.save(entry));
    }

    @Cacheable(value = "monthlySettlement", key = "#year + '-' + #month + '-' + (#siteId != null ? #siteId : 'all')")
    public MonthlySettlementReportDto generateMonthlySettlement(int year, int month, Long siteId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<TimeEntry> entries = timeEntryRepository.findByClockInBetween(start, end).stream()
                .filter(te -> te.getClockOut() != null)
                .filter(te -> siteId == null || te.getWorkSchedule().getSite().getId().equals(siteId))
                .collect(Collectors.toList());

        MonthlySettlementReportDto report = new MonthlySettlementReportDto();
        report.setPeriod(yearMonth.toString());
        if (siteId != null) {
            ConstructionSite site = constructionSiteRepository.findById(siteId)
                    .orElseThrow(() -> new IllegalArgumentException("Site not found: " + siteId));
            report.setSiteId(siteId);
            report.setSiteName(site.getName());
        }

        Map<Long, List<TimeEntry>> byEmployee = entries.stream()
                .collect(Collectors.groupingBy(te -> te.getWorkSchedule().getEmployee().getId()));

        for (Map.Entry<Long, List<TimeEntry>> group : byEmployee.entrySet()) {
            Employee employee = group.getValue().get(0).getWorkSchedule().getEmployee();
            MonthlySettlementReportDto.WorkerSettlementLineDto line = new MonthlySettlementReportDto.WorkerSettlementLineDto();
            line.setEmployeeId(employee.getId());
            line.setEmployeeName(employee.getFullName());

            Set<LocalDate> days = new HashSet<>();
            BigDecimal reg = BigDecimal.ZERO;
            BigDecimal ot = BigDecimal.ZERO;
            BigDecimal dt = BigDecimal.ZERO;

            for (TimeEntry te : group.getValue()) {
                days.add(te.getWorkSchedule().getDate());
                reg = reg.add(te.getRegularHours());
                ot = ot.add(te.getOvertimeHours());
                dt = dt.add(te.getDoubleTimeHours());
            }

            line.setDaysWorked(days.size());
            line.setRegularHours(reg);
            line.setOvertimeHours(ot);
            line.setDoubleTimeHours(dt);

            BigDecimal hourly = employee.getHourlyRate();
            BigDecimal otRate = employee.getOvertimeRate();
            line.setRegularPay(reg.multiply(hourly));
            line.setOvertimePay(ot.multiply(otRate));
            line.setDoubleTimePay(dt.multiply(hourly.multiply(new BigDecimal("2.0"))));
            line.setTotalPay(line.getRegularPay().add(line.getOvertimePay()).add(line.getDoubleTimePay()));

            report.getWorkers().add(line);
            report.setTotalRegularHours(report.getTotalRegularHours().add(reg));
            report.setTotalOvertimeHours(report.getTotalOvertimeHours().add(ot));
            report.setTotalDoubleTimeHours(report.getTotalDoubleTimeHours().add(dt));
            report.setTotalRegularPay(report.getTotalRegularPay().add(line.getRegularPay()));
            report.setTotalOvertimePay(report.getTotalOvertimePay().add(line.getOvertimePay()));
            report.setTotalDoubleTimePay(report.getTotalDoubleTimePay().add(line.getDoubleTimePay()));
            report.setGrandTotalPay(report.getGrandTotalPay().add(line.getTotalPay()));
        }

        report.setTotalWorkers(report.getWorkers().size());
        report.getWorkers().sort(Comparator.comparing(MonthlySettlementReportDto.WorkerSettlementLineDto::getEmployeeName));
        return report;
    }

    private ShiftType resolveShiftType(Employee employee, Long shiftTypeId) {
        if (shiftTypeId != null) {
            return shiftTypeRepository.findById(shiftTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Shift type not found: " + shiftTypeId));
        }
        if (employee.getDefaultShiftType() != null) {
            return employee.getDefaultShiftType();
        }
        return shiftTypeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No shift types configured. Create a shift type before clock-in."));
    }

    private void verifySiteExists(Long siteId) {
        if (!constructionSiteRepository.existsById(siteId)) {
            throw new IllegalArgumentException("Site not found: " + siteId);
        }
    }

    private AttendanceRecordDto toDto(TimeEntry entry) {
        WorkSchedule schedule = entry.getWorkSchedule();
        Employee employee = schedule.getEmployee();
        ConstructionSite site = schedule.getSite();
        ShiftType shift = schedule.getShiftType();

        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setTimeEntryId(entry.getId());
        dto.setEmployeeId(employee.getId());
        dto.setEmployeeName(employee.getFullName());
        dto.setSiteId(site.getId());
        dto.setSiteName(site.getName());
        dto.setShiftTypeId(shift.getId());
        dto.setShiftName(shift.getName());
        dto.setWorkDate(schedule.getDate());
        dto.setClockIn(entry.getClockIn());
        dto.setClockOut(entry.getClockOut());
        dto.setBreakMinutes(entry.getBreakMinutes());
        dto.setRegularHours(entry.getRegularHours());
        dto.setOvertimeHours(entry.getOvertimeHours());
        dto.setDoubleTimeHours(entry.getDoubleTimeHours());
        dto.setOnSite(entry.isOnSite());
        dto.setApproved(entry.getIsApproved());
        return dto;
    }

    // Worker-based attendance methods (using Worker/Site/AttendanceLog model)
    @Transactional
    public AttendanceRecordDto clockIn(Long workerId, Long siteId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException(workerId));
        if (worker.getStatus() != ActiveStatus.ACTIVE) {
            throw new IllegalStateException("Only active workers can clock in");
        }

        attendanceLogRepository.findOpenEntryByWorkerId(workerId).ifPresent(entry -> {
            throw new DuplicateClockInException(entry.getSite().getName());
        });

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new SiteNotFoundException(siteId));
        if (site.getStatus() != ActiveStatus.ACTIVE) {
            throw new IllegalStateException("Site is not active");
        }

        LocalDateTime clockInTime = LocalDateTime.now();
        
        // Validate clock-in time is not in the future
        if (clockInTime.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new InvalidClockInTimeException("Clock-in time cannot be in the future");
        }

        AttendanceLog log = new AttendanceLog(worker, site, clockInTime);
        log = attendanceLogRepository.save(log);

        // Add to Redis cache with 16-hour TTL
        activeWorkersCacheService.addActiveWorker(
            workerId,
            siteId,
            site.getName(),
            worker.getName(),
            log.getClockIn().toString()
        );

        return toDto(log);
    }

    @Transactional
    public AttendanceRecordDto clockOut(Long workerId) {
        AttendanceLog log = attendanceLogRepository.findOpenEntryByWorkerId(workerId)
                .orElseThrow(() -> new NoOpenClockInException(workerId));

        LocalDateTime clockOutTime = LocalDateTime.now();
        log.setClockOut(clockOutTime);

        long totalMinutes = Duration.between(log.getClockIn(), clockOutTime).toMinutes();
        double totalHours = totalMinutes / 60.0;

        // Calculate overtime (assuming 8 hours is regular time)
        double regularHours = Math.min(totalHours, 8.0);
        double overtimeHours = Math.max(totalHours - 8.0, 0.0);

        log.setTotalHours(BigDecimal.valueOf(regularHours).setScale(2, RoundingMode.HALF_UP));
        log.setOvertimeHours(BigDecimal.valueOf(overtimeHours).setScale(2, RoundingMode.HALF_UP));

        // Flag for review if shift exceeds 16 hours
        if (totalHours > 16.0) {
            log.setFlaggedForReview(true);
        }

        log = attendanceLogRepository.save(log);

        // Create overtime entry with tiered rate calculation
        if (overtimeHours > 0) {
            createOvertimeEntry(log, overtimeHours);
        }

        // Remove from Redis cache
        activeWorkersCacheService.removeActiveWorker(workerId);

        return toDto(log);
    }

    private void createOvertimeEntry(AttendanceLog log, double overtimeHours) {
        Worker worker = log.getWorker();
        BigDecimal dailyWageRate = worker.getDailyWageRate();
        
        // Calculate tiered overtime rates
        // First 2 hours: 1.5x daily wage rate
        // Beyond 2 hours: 2x daily wage rate
        double firstTierHours = Math.min(overtimeHours, 2.0);
        double secondTierHours = Math.max(overtimeHours - 2.0, 0.0);
        
        BigDecimal firstTierRate = dailyWageRate.multiply(new BigDecimal("1.5"));
        BigDecimal secondTierRate = dailyWageRate.multiply(new BigDecimal("2.0"));
        
        BigDecimal firstTierAmount = firstTierRate.multiply(BigDecimal.valueOf(firstTierHours));
        BigDecimal secondTierAmount = secondTierRate.multiply(BigDecimal.valueOf(secondTierHours));
        BigDecimal totalAmount = firstTierAmount.add(secondTierAmount);
        
        // Check monthly overtime cap (60 hours)
        YearMonth currentMonth = YearMonth.from(log.getClockIn());
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        
        // Get existing overtime for the month
        List<com.payrollapplication.payroll.model.OvertimeEntry> existingEntries = 
            overtimeEntryRepository.findByWorkerIdAndDateRange(worker.getId(), monthStart, monthEnd);
        
        BigDecimal existingOvertimeHours = existingEntries.stream()
            .map(com.payrollapplication.payroll.model.OvertimeEntry::getOvertimeHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal newOvertimeHours = BigDecimal.valueOf(overtimeHours);
        BigDecimal totalMonthlyOvertime = existingOvertimeHours.add(newOvertimeHours);
        
        // Cap at 60 hours
        BigDecimal monthlyCap = new BigDecimal("60.0");
        if (totalMonthlyOvertime.compareTo(monthlyCap) > 0) {
            BigDecimal cappedHours = monthlyCap.subtract(existingOvertimeHours);
            if (cappedHours.compareTo(BigDecimal.ZERO) <= 0) {
                // Already at cap, don't create entry
                return;
            }
            
            // Recalculate amount with capped hours
            double cappedOvertimeHours = cappedHours.doubleValue();
            firstTierHours = Math.min(cappedOvertimeHours, 2.0);
            secondTierHours = Math.max(cappedOvertimeHours - 2.0, 0.0);
            
            firstTierAmount = firstTierRate.multiply(BigDecimal.valueOf(firstTierHours));
            secondTierAmount = secondTierRate.multiply(BigDecimal.valueOf(secondTierHours));
            totalAmount = firstTierAmount.add(secondTierAmount);
            newOvertimeHours = cappedHours;
        }
        
        // Create overtime entry
        com.payrollapplication.payroll.model.OvertimeEntry overtimeEntry = 
            new com.payrollapplication.payroll.model.OvertimeEntry();
        overtimeEntry.setWorker(worker);
        overtimeEntry.setAttendanceLog(log);
        overtimeEntry.setDate(log.getClockIn().toLocalDate());
        overtimeEntry.setOvertimeHours(newOvertimeHours);
        overtimeEntry.setOvertimeRate(firstTierRate); // Store first tier as base rate
        overtimeEntry.setAmount(totalAmount);
        overtimeEntry.setSettlementStatus(com.payrollapplication.payroll.model.SettlementStatus.PENDING);
        
        overtimeEntryRepository.save(overtimeEntry);
    }

    public List<AttendanceRecordDto> getActiveWorkers() {
        // Read exclusively from Redis
        Set<Long> activeWorkerIds = activeWorkersCacheService.getAllActiveWorkerIds();
        return activeWorkerIds.stream()
                .map(workerId -> {
                    Map<String, Object> workerData = activeWorkersCacheService.getActiveWorkerData(workerId);
                    if (workerData.isEmpty()) {
                        return null;
                    }
                    AttendanceRecordDto dto = new AttendanceRecordDto();
                    dto.setEmployeeId(((Number) workerData.get("workerId")).longValue());
                    dto.setEmployeeName((String) workerData.get("workerName"));
                    dto.setSiteId(((Number) workerData.get("siteId")).longValue());
                    dto.setSiteName((String) workerData.get("siteName"));
                    dto.setClockIn(LocalDateTime.parse((String) workerData.get("clockInTime")));
                    dto.setOnSite(true);
                    dto.setApproved(true);
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public PaginatedResponseDto<AttendanceRecordDto> getWorkerAttendance(Long workerId, LocalDate from, LocalDate to, int page, int size) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be on or before to date");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("clockIn").ascending());
        
        org.springframework.data.domain.Page<AttendanceLog> logsPage = 
            attendanceLogRepository.findByWorkerIdAndClockInBetweenWithJoins(workerId, start, end, pageable);

        List<AttendanceRecordDto> dtos = logsPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(dtos, page, size, logsPage.getTotalElements());
    }

    private AttendanceRecordDto toDto(AttendanceLog log) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setTimeEntryId(log.getId());
        dto.setEmployeeId(log.getWorker().getId());
        dto.setEmployeeName(log.getWorker().getName());
        dto.setSiteId(log.getSite().getId());
        dto.setSiteName(log.getSite().getName());
        dto.setClockIn(log.getClockIn());
        dto.setClockOut(log.getClockOut());
        dto.setRegularHours(log.getTotalHours());
        dto.setOvertimeHours(log.getOvertimeHours());
        dto.setOnSite(log.getClockOut() == null);
        dto.setApproved(true);
        dto.setWorkDate(log.getClockIn().toLocalDate());
        return dto;
    }
}
