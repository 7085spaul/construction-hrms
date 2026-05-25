package com.payrollapplication.payroll.exception;

public class SiteNotFoundException extends RuntimeException {
    private final Long siteId;

    public SiteNotFoundException(Long siteId) {
        super("Site not found: " + siteId);
        this.siteId = siteId;
    }

    public Long getSiteId() {
        return siteId;
    }
}
