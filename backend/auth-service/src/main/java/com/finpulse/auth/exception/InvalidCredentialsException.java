package com.finpulse.auth.exception;

import org.springframework.http.HttpStatus;

/*
Metodo criado por: Pedro Queiroz
Projeto de estudos
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos", HttpStatus.UNAUTHORIZED);
    }
}