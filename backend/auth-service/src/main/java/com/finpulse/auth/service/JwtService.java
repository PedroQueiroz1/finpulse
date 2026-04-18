package com.finpulse.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/*
Esse é o serviço que gera e valida tokens JWT

JWT é um token codificado em Base64 com três partes: 
- Header (algoritmo), 
- Payload (dados do usuário — chamados "claims")
- Signature (assinatura). 
O servidor gera o token no login e o cliente envia em cada requisição no header Authorization: Bearer <token>. 
O servidor valida a assinatura sem precisar consultar o banco.
Design Pattern aqui — Strategy: O @Value permite trocar o secret e tempos de expiração via configuração sem alterar código. Isso é o princípio O do SOLID (Open/Closed) — aberto para extensão, fechado para modificação.

ATENÇÃO: extractClaim foi alterado de private para public para o JwtAuthenticationFilter poder acessar.

Metodo criado por Pedro Queiroz
Projeto de estudos
*/
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret:finpulse-secret-key-que-deve-ter-pelo-menos-256-bits-para-ser-segura}")
    private String secretKey;

    @Value("${jwt.access-token-expiration:900000}")   // 15 minutos em milissegundos
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 dias em milissegundos
    private long refreshTokenExpiration;

    // Gera Access Token
    public String generateAccessToken(String email, Map<String, Object> extraClaims) {
        return buildToken(email, extraClaims, accessTokenExpiration);
    }

    // Gera Refresh Token (sem claims extras, mais leve)
    public String generateRefreshToken(String email) {
        return buildToken(email, Map.of(), refreshTokenExpiration);
    }

    // Extrai o e-mail (subject) do token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Valida se o token é válido e pertence ao usuário
    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado para: {}", email);
            return false;
        } catch (MalformedJwtException | UnsupportedJwtException | SecurityException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // ============================================================
    // Métodos privados
    // ============================================================

    private String buildToken(String subject, Map<String, Object> extraClaims, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Private --> Public por causa do JwtAuthenticationFilter!
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}