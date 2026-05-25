package com.payrollapplication.payroll.controller;

import com.payrollapplication.payroll.dto.OvertimeSummaryDto;
import com.payrollapplication.payroll.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/overtime")
@Validated
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    @GetMapping("/summary/{workerId}")
    public ResponseEntity<OvertimeSummaryDto> getMonthlySummary(
            @PathVariable Long workerId,
            @RequestParam @NotNull(message = "Month parameter is required") String month) {
        return ResponseEntity.ok(overtimeService.getMonthlySummary(workerId, month));
    }

    @PostMapping("/settle/{workerId}")
    public ResponseEntity<Map<String, Object>> settleOvertime(
            @PathVariable Long workerId,
            @RequestParam @NotNull(message = "Month parameter is required") String month) {
        BigDecimal totalAmount = overtimeService.settleOvertime(workerId, month);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Overtime settled successfully");
        response.put("totalAmount", totalAmount);
        response.put("workerId", workerId);
        response.put("month", month);
        return ResponseEntity.ok(response);
    }
}
