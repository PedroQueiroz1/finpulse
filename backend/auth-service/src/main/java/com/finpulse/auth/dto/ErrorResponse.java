package com.finpulse.auth.dto;

import java.time.LocalDateTime;
import java.util.Map;

/*
Metodo criado por: Pedro Queiroz
Projeto de estudos
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    // Construtor simplificado para erros sem validação
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }
}