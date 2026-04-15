package com.finpulse.auth.exception;

import org.springframework.http.HttpStatus;

/*
Metodo criado por: Pedro Queiroz
Projeto de estudos
 */
public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super("E-mail já cadastrado: " + email, HttpStatus.CONFLICT);
    }
}