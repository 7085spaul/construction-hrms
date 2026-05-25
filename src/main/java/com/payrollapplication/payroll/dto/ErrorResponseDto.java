package com.payrollapplication.payroll.dto;

import java.time.LocalDateTime;

public class ErrorResponseDto {

    private String error;
    private String message;
    private String timestamp;

    public ErrorResponseDto() {
    }

    public ErrorResponseDto(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
