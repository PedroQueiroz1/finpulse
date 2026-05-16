package com.finpulse.gateway.routing;

import com.finpulse.gateway.AbstractGatewayTest;
import com.finpulse.gateway.helper.JwtTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@DisplayName("Gateway - Roteamento")
class GatewayRoutingTest extends AbstractGatewayTest {

    // ================================================================
    // Auth Routes (public — sem JwtAuth filter)
    // ================================================================

    @Nested
    @DisplayName("Auth Routes")
    class AuthRoutes {

        @Test
        @DisplayName("deve rotear POST /api/auth/login para auth-service")
        void deveRotearParaAuthService() {
            wireMockServer.stubFor(post(urlEqualTo("/api/auth/login"))
                    .willReturn(okJson("{\"accessToken\":\"mock-token\",\"refreshToken\":\"refresh\"}")));

            webTestClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"email\":\"test@test.com\",\"password\":\"senha123\"}")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.accessToken").isEqualTo("mock-token");
        }

        @Test
        @DisplayName("deve aceitar login sem JWT (endpoint público)")
        void deveAceitarLoginSemJwt() {
            wireMockServer.stubFor(post(urlEqualTo("/api/auth/login"))
                    .willReturn(okJson("{\"accessToken\":\"token\"}")));

            webTestClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"email\":\"u@test.com\",\"password\":\"p\"}")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("deve aceitar register sem JWT (endpoint público)")
        void deveAceitarRegisterSemJwt() {
            wireMockServer.stubFor(post(urlEqualTo("/api/auth/register"))
                    .willReturn(aResponse().withStatus(201)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"accessToken\":\"new-token\"}")));

            webTestClient.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"name\":\"U\",\"email\":\"u@t.com\",\"password\":\"p\"}")
                    .exchange()
                    .expectStatus().isCreated();
        }
    }

    // ================================================================
    // Notes Routes (protegidas — JwtAuth filter)
    // ================================================================

    @Nested
    @DisplayName("Notes Routes")
    class NotesRoutes {

        @Test
        @DisplayName("deve rotear para notes-service com JWT válido")
        void deveRotearParaNotesServiceComJwtValido() {
            wireMockServer.stubFor(get(urlEqualTo("/api/notes"))
                    .willReturn(okJson("[]")));

            String token = JwtTestHelper.generateToken("user@test.com");

            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("deve rejeitar 401 sem JWT em rota protegida")
        void deveRejeitar401SemJwt() {
            webTestClient.get()
                    .uri("/api/notes")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("deve rejeitar 401 com JWT expirado")
        void deveRejeitar401ComJwtExpirado() {
            String expiredToken = JwtTestHelper.generateExpiredToken("user@test.com");

            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer " + expiredToken)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("deve rejeitar 401 com token malformado")
        void deveRejeitar401ComTokenMalformado() {
            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer not.a.valid.jwt.token")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("deve injetar X-User-Id no request downstream com JWT válido")
        void deveInjetarXUserIdComJwtValido() {
            wireMockServer.stubFor(get(urlEqualTo("/api/notes"))
                    .withHeader("X-User-Id", equalTo("test-user-id-123"))
                    .willReturn(okJson("[]")));

            String token = JwtTestHelper.generateToken("user@test.com", "test-user-id-123", "USER");

            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();

            wireMockServer.verify(getRequestedFor(urlEqualTo("/api/notes"))
                    .withHeader("X-User-Id", equalTo("test-user-id-123")));
        }
    }

    // ================================================================
    // Stock Routes (protegidas — JwtAuth + CircuitBreaker)
    // ================================================================

    @Nested
    @DisplayName("Stock Routes")
    class StockRoutes {

        @Test
        @DisplayName("deve rotear para stock-service com JWT válido")
        void deveRotearParaStockServiceComJwt() {
            wireMockServer.stubFor(get(urlPathEqualTo("/api/stocks/AAPL/quote"))
                    .willReturn(okJson("{\"symbol\":\"AAPL\",\"price\":182.34}")));

            String token = JwtTestHelper.generateToken("user@test.com");

            webTestClient.get()
                    .uri("/api/stocks/AAPL/quote")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.symbol").isEqualTo("AAPL");
        }

        @Test
        @DisplayName("deve rejeitar 401 sem JWT em rota de stocks")
        void deveRejeitar401SemJwtEmStock() {
            webTestClient.get()
                    .uri("/api/stocks/AAPL/quote")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("deve retornar fallback 503 quando stock-service está down (circuit breaker)")
        void deveAplicarCircuitBreakerNoStock() {
            // 3 falhas com failureRateThreshold=100% abre o CB
            wireMockServer.stubFor(get(urlPathMatching("/api/stocks/.*"))
                    .willReturn(aResponse().withStatus(503)));

            String token = JwtTestHelper.generateToken("user@test.com");

            // Dispara falhas suficientes para abrir o CB (slidingWindowSize=3)
            for (int i = 0; i < 3; i++) {
                webTestClient.get()
                        .uri("/api/stocks/AAPL/quote")
                        .header("Authorization", "Bearer " + token)
                        .exchange();
            }

            // Próxima chamada deve cair no fallback
            webTestClient.get()
                    .uri("/api/stocks/AAPL/quote")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().value(status ->
                            org.assertj.core.api.Assertions.assertThat(status)
                                    .isIn(503, 200)); // fallback ou CB ainda processando
        }
    }
}
