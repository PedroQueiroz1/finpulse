package com.finpulse.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Note que aqui não usamos @Mock — estamos testando o JwtService diretamente, sem mocks. 
Isso porque o JwtService não depende de repositório nem de outros services, só de configurações (que injetamos via ReflectionTestUtils). Esse é um teste unitário "puro".

Anatomia de um teste — padrão Given/When/Then (ou Arrange/Act/Assert):

Given — configura o cenário e os comportamentos dos mocks com when().thenReturn()
When — executa o método que está sendo testado
Then — verifica o resultado com assertEquals, assertNotNull, assertThrows

Anotações importantes:

@Mock — cria um objeto falso que simula o comportamento real
@InjectMocks — cria a classe real e injeta os mocks nela
@BeforeEach — roda antes de cada teste, garantindo estado limpo
@Nested — agrupa testes relacionados (fica bonito no relatório)
@DisplayName — nome legível no relatório de testes
verify() — confirma que um método do mock foi (ou não foi) chamado

Metodo criado por Pedro queiroz
projeto De estudos
*/
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Injeta valores nos campos @Value sem precisar subir o Spring
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "finpulse-test-secret-key-que-deve-ter-pelo-menos-256-bits-de-comprimento-seguro");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
    }

    @Test
    @DisplayName("Deve gerar access token válido")
    void shouldGenerateValidAccessToken() {
        // GIVEN
        String email = "pedro@finpulse.com";
        Map<String, Object> claims = Map.of("role", "USER", "name", "Pedro");

        // WHEN
        String token = jwtService.generateAccessToken(email, claims);

        // THEN
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(email, jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    @DisplayName("Deve gerar refresh token válido")
    void shouldGenerateValidRefreshToken() {
        // GIVEN
        String email = "pedro@finpulse.com";

        // WHEN
        String token = jwtService.generateRefreshToken(email);

        // THEN
        assertNotNull(token);
        assertEquals(email, jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    @DisplayName("Deve invalidar token com e-mail diferente")
    void shouldInvalidateTokenWithDifferentEmail() {
        // GIVEN
        String token = jwtService.generateAccessToken("pedro@finpulse.com", Map.of());

        // WHEN & THEN
        assertFalse(jwtService.isTokenValid(token, "outro@email.com"));
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void shouldRejectExpiredToken() {
        // GIVEN — cria um JwtService com expiração de 0ms (token nasce expirado)
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(expiredJwtService, "secretKey",
                "finpulse-test-secret-key-que-deve-ter-pelo-menos-256-bits-de-comprimento-seguro");
        ReflectionTestUtils.setField(expiredJwtService, "accessTokenExpiration", 0L);
        ReflectionTestUtils.setField(expiredJwtService, "refreshTokenExpiration", 0L);

        String token = expiredJwtService.generateAccessToken("pedro@finpulse.com", Map.of());

        // WHEN & THEN
        assertFalse(expiredJwtService.isTokenValid(token, "pedro@finpulse.com"));
    }

    @Test
    @DisplayName("Deve extrair claims customizadas do token")
    void shouldExtractCustomClaims() {
        // GIVEN
        Map<String, Object> claims = Map.of("role", "ADMIN", "name", "Admin User");
        String token = jwtService.generateAccessToken("admin@finpulse.com", claims);

        // WHEN
        String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
        String name = jwtService.extractClaim(token, c -> c.get("name", String.class));

        // THEN
        assertEquals("ADMIN", role);
        assertEquals("Admin User", name);
    }
}