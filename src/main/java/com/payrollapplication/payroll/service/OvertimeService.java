package com.payrollapplication.payroll.service;

import com.payrollapplication.payroll.dto.OvertimeSummaryDto;
import com.payrollapplication.payroll.event.OvertimeSettlementEvent;
import com.payrollapplication.payroll.exception.AlreadySettledException;
import com.payrollapplication.payroll.exception.WorkerNotFoundException;
import com.payrollapplication.payroll.model.OvertimeEntry;
import com.payrollapplication.payroll.model.SettlementStatus;
import com.payrollapplication.payroll.model.Worker;
import com.payrollapplication.payroll.repository.OvertimeEntryRepository;
import com.payrollapplication.payroll.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OvertimeService {

    @Autowired
    private OvertimeEntryRepository overtimeEntryRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public OvertimeSummaryDto getMonthlySummary(Long workerId, String month) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException(workerId));

        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<OvertimeEntry> entries = overtimeEntryRepository.findByWorkerIdAndDateRange(workerId, start, end);

        OvertimeSummaryDto summary = new OvertimeSummaryDto();
        summary.setWorkerId(workerId);
        summary.setWorkerName(worker.getName());
        summary.setMonth(month);

        List<OvertimeSummaryDto.OvertimeBreakdownDto> breakdown = entries.stream()
                .map(entry -> {
                    OvertimeSummaryDto.OvertimeBreakdownDto dto = new OvertimeSummaryDto.OvertimeBreakdownDto();
                    dto.setDate(entry.getDate().toString());
                    dto.setOvertimeHours(entry.getOvertimeHours());
                    dto.setOvertimeRate(entry.getOvertimeRate());
                    dto.setAmount(entry.getAmount());
                    dto.setSettlementStatus(entry.getSettlementStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());

        BigDecimal totalHours = entries.stream()
                .map(OvertimeEntry::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = entries.stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.setTotalOvertimeHours(totalHours);
        summary.setTotalPayoutAmount(totalAmount);
        summary.setBreakdown(breakdown);

        // Determine overall settlement status
        boolean allSettled = entries.stream().allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED);
        boolean anyPending = entries.stream().anyMatch(e -> e.getSettlementStatus() == SettlementStatus.PENDING);
        
        if (entries.isEmpty()) {
            summary.setSettlementStatus("NO_ENTRIES");
        } else if (allSettled) {
            summary.setSettlementStatus("SETTLED");
        } else if (anyPending) {
            summary.setSettlementStatus("PARTIALLY_SETTLED");
        } else {
            summary.setSettlementStatus("PENDING");
        }

        return summary;
    }

    @Transactional
    public java.math.BigDecimal settleOvertime(Long workerId, String month) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException(workerId));

        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // Cannot settle current month
        YearMonth currentMonth = YearMonth.now();
        if (yearMonth.equals(currentMonth) || yearMonth.isAfter(currentMonth)) {
            throw new IllegalStateException("Cannot settle current or future months. Only past months can be settled.");
        }

        List<OvertimeEntry> entries = overtimeEntryRepository.findByWorkerIdAndDateRange(workerId, start, end);

        if (entries.isEmpty()) {
            throw new IllegalStateException("No overtime entries found for worker " + workerId + " in month " + month);
        }

        // Check if any entries are already settled
        boolean alreadySettled = entries.stream().anyMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED);
        if (alreadySettled) {
            throw new AlreadySettledException(workerId, month);
        }

        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

        // Process all entries in a single transaction
        for (OvertimeEntry entry : entries) {
            entry.setSettlementStatus(SettlementStatus.SETTLED);
            totalAmount = totalAmount.add(entry.getAmount());
            overtimeEntryRepository.save(entry);
        }

        // Publish event for SMS notification - will only fire AFTER transaction commits
        eventPublisher.publishEvent(new OvertimeSettlementEvent(this, workerId, month, totalAmount, worker.getName()));

        return totalAmount;
    }
}
