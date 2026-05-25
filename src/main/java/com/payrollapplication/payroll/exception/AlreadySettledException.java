package com.payrollapplication.payroll.exception;

public class AlreadySettledException extends RuntimeException {
    private final Long workerId;
    private final String month;

    public AlreadySettledException(Long workerId, String month) {
        super("Some overtime entries are already settled and cannot be modified for worker " + workerId + " in month " + month);
        this.workerId = workerId;
        this.month = month;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public String getMonth() {
        return month;
    }
}
