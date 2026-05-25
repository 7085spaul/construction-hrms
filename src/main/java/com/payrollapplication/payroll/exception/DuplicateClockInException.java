package com.payrollapplication.payroll.exception;

public class DuplicateClockInException extends RuntimeException {
    private final String siteName;

    public DuplicateClockInException(String siteName) {
        super("Worker is already clocked in at Site: " + siteName);
        this.siteName = siteName;
    }

    public String getSiteName() {
        return siteName;
    }
}
