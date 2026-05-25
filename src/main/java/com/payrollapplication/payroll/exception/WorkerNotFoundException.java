package com.payrollapplication.payroll.exception;

public class WorkerNotFoundException extends RuntimeException {
    private final Long workerId;

    public WorkerNotFoundException(Long workerId) {
        super("Worker not found: " + workerId);
        this.workerId = workerId;
    }

    public Long getWorkerId() {
        return workerId;
    }
}
