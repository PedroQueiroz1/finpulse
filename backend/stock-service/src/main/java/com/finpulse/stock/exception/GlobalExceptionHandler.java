package com.finpulse.stock.exception;

import com.finpulse.stock.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStockNotFound(
            StockNotFoundException ex, HttpServletRequest request) {
        log.warn("Stock not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(404, "Not Found", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(
            ExternalApiException ex, HttpServletRequest request) {
        log.error("External API error [{}]: {}", ex.getProvider(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                new ErrorResponse(502, "Bad Gateway",
                        "Erro ao consultar API externa: " + ex.getMessage(),
                        request.getRequestURI())
        );
    }

    @ExceptionHandler(ProviderNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleProviderNotAvailable(
            ProviderNotAvailableException ex, HttpServletRequest request) {
        log.warn("Provider not available: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new ErrorResponse(503, "Service Unavailable",
                        ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(500, "Internal Server Error",
                        "Erro interno", request.getRequestURI())
        );
    }
}