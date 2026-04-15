package com.finpulse.auth.exception;

import org.springframework.http.HttpStatus;

/*
Metodo criado por: Pedro Queiroz
Projeto de estudos
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}