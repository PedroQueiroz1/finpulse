package com.finpulse.auth.dto;

/*
Anotação de estudo:
Os "record" eles carregam praticamente os mesmos dados que o User.java por exemplo, que é uma entidade.
O que isso quer dizer? Quer dizer que o "record" gera automaticamente o construtor e os getters.
A diferença e muito importante é que os campos são sempre "final", ou seja, são atributos de um objeto imutável.
Campos final só podem ser definidos no momento da criação...
*/
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}