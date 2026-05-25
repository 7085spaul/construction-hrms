package com.payrollapplication.payroll.exception;

import com.payrollapplication.payroll.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateClockInException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateClockIn(DuplicateClockInException ex) {
        ErrorResponseDto error = new ErrorResponseDto("DUPLICATE_CLOCK_IN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(WorkerNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleWorkerNotFound(WorkerNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto("WORKER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SiteNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSiteNotFound(SiteNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto("SITE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoOpenClockInException.class)
    public ResponseEntity<ErrorResponseDto> handleNoOpenClockIn(NoOpenClockInException ex) {
        ErrorResponseDto error = new ErrorResponseDto("NO_OPEN_CLOCK_IN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AlreadySettledException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadySettled(AlreadySettledException ex) {
        ErrorResponseDto error = new ErrorResponseDto("ALREADY_SETTLED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidClockInTimeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidClockInTime(InvalidClockInTimeException ex) {
        ErrorResponseDto error = new ErrorResponseDto("INVALID_CLOCK_IN_TIME", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(IllegalArgumentException ex) {
        ErrorResponseDto error = new ErrorResponseDto("BAD_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(IllegalStateException ex) {
        ErrorResponseDto error = new ErrorResponseDto("CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorResponseDto error = new ErrorResponseDto("VALIDATION_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntime(RuntimeException ex) {
        ErrorResponseDto error = new ErrorResponseDto("INTERNAL_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
