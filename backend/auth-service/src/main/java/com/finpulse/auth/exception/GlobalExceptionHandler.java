package com.finpulse.auth.exception;

import com.finpulse.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
Design Patterns aplicados:

Template Method — BusinessException é a classe base, as filhas (EmailAlreadyExistsException, etc.) personalizam mensagem e status
Chain of Responsibility — o @RestControllerAdvice funciona como uma cadeia: tenta o handler mais específico primeiro (BusinessException), depois o de validação, e por último o genérico

A anotação @RestControllerAdvice intercepta todas as exceções de todos os controllers automaticamente. Sem ela, cada controller teria que tratar seus próprios erros.

Metodo criado por: Pedro Queiroz
Projeto de estudos
*/
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Trata todas as BusinessExceptions (e suas filhas)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Trata erros de validação (@Valid nos DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        log.warn("Validation failed: {} - Path: {}", validationErrors, request.getRequestURI());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Erro de validação nos campos enviados",
                request.getRequestURI(),
                LocalDateTime.now(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(error);
    }

    // Trata qualquer exceção não prevista (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Erro interno do servidor",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}