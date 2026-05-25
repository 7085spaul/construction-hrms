package com.payrollapplication.payroll.exception;

public class NoOpenClockInException extends RuntimeException {
    private final Long workerId;

    public NoOpenClockInException(Long workerId) {
        super("No open clock-in for worker: " + workerId);
        this.workerId = workerId;
    }

    public Long getWorkerId() {
        return workerId;
    }
}
