package com.payrollapplication.payroll.controller;

import com.payrollapplication.payroll.dto.AttendanceRecordDto;
import com.payrollapplication.payroll.dto.PaginatedResponseDto;
import com.payrollapplication.payroll.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@Validated
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordDto> clockIn(@Valid @RequestBody ClockInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                attendanceService.clockIn(request.getWorkerId(), request.getSiteId()));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordDto> clockOut(@Valid @RequestBody ClockOutRequest request) {
        return ResponseEntity.ok(
                attendanceService.clockOut(request.getWorkerId()));
    }

    @GetMapping("/active")
    public ResponseEntity<java.util.List<AttendanceRecordDto>> getActiveWorkers() {
        return ResponseEntity.ok(attendanceService.getActiveWorkers());
    }

    @GetMapping("/log")
    public ResponseEntity<PaginatedResponseDto<AttendanceRecordDto>> getWorkerAttendance(
            @RequestParam Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(attendanceService.getWorkerAttendance(workerId, from, to, page, size));
    }

    public static class ClockInRequest {
        @NotNull(message = "Worker ID is required")
        private Long workerId;
        @NotNull(message = "Site ID is required")
        private Long siteId;

        public Long getWorkerId() { return workerId; }
        public void setWorkerId(Long workerId) { this.workerId = workerId; }
        public Long getSiteId() { return siteId; }
        public void setSiteId(Long siteId) { this.siteId = siteId; }
    }

    public static class ClockOutRequest {
        @NotNull(message = "Worker ID is required")
        private Long workerId;

        public Long getWorkerId() { return workerId; }
        public void setWorkerId(Long workerId) { this.workerId = workerId; }
    }
}