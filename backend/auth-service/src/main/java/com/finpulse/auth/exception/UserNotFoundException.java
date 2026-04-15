package com.finpulse.auth.exception;

import org.springframework.http.HttpStatus;

/*
Metodo criado por: Pedro Queiroz
Projeto de estudos
*/
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String identifier) {
        super("Usuário não encontrado: " + identifier, HttpStatus.NOT_FOUND);
    }
}