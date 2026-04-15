package com.finpulse.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
Anotação de estudo:
Os "record" eles carregam praticamente os mesmos dados que o User.java por exemplo, que é uma entidade.
O que isso quer dizer? Quer dizer que o "record" gera automaticamente o construtor e os getters.
A diferença e muito importante é que os campos são sempre "final", ou seja, são atributos de um objeto imutável.
Campos final só podem ser definidos no momento da criação...

Metodo criado por: Pedro Queiroz
Projeto de estudos
*/
public record LoginRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @NotBlank
        String password
) {}