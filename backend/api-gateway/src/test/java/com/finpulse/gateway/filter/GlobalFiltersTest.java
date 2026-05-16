package com.finpulse.gateway.filter;

import com.finpulse.gateway.AbstractGatewayTest;
import com.finpulse.gateway.helper.JwtTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gateway - Filtros Globais")
class GlobalFiltersTest extends AbstractGatewayTest {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    // ================================================================
    // Correlation ID
    // ================================================================

    @Nested
    @DisplayName("Correlation ID Filter")
    class CorrelationIdFilterTests {

        @Test
        @DisplayName("deve gerar Correlation-ID quando header ausente")
        void deveGerarCorrelationIdQuandoHeaderAusente() {
            wireMockServer.stubFor(get(urlEqualTo("/api/auth/health"))
                    .willReturn(okJson("{\"status\":\"UP\"}")));

            webTestClient.get()
                    .uri("/api/auth/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists(CORRELATION_ID_HEADER)
                    .expectHeader().value(CORRELATION_ID_HEADER,
                            cid -> assertThat(cid).isNotBlank().hasSize(36)); // UUID v4
        }

        @Test
        @DisplayName("deve propagar Correlation-ID fornecido pelo cliente")
        void devePropagarCorrelationIdFornecido() {
            String existingCid = "abc123-fixed-correlation-id";

            wireMockServer.stubFor(get(urlEqualTo("/api/auth/health"))
                    .withHeader(CORRELATION_ID_HEADER, equalTo(existingCid))
                    .willReturn(okJson("{\"status\":\"UP\"}")));

            webTestClient.get()
                    .uri("/api/auth/health")
                    .header(CORRELATION_ID_HEADER, existingCid)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueEquals(CORRELATION_ID_HEADER, existingCid);

            wireMockServer.verify(getRequestedFor(urlEqualTo("/api/auth/health"))
                    .withHeader(CORRELATION_ID_HEADER, equalTo(existingCid)));
        }

        @Test
        @DisplayName("deve devolver Correlation-ID na resposta")
        void deveDevolverCorrelationIdNaResposta() {
            wireMockServer.stubFor(get(urlEqualTo("/api/auth/health"))
                    .willReturn(okJson("{\"status\":\"UP\"}")));

            webTestClient.get()
                    .uri("/api/auth/health")
                    .exchange()
                    .expectHeader().exists(CORRELATION_ID_HEADER);
        }
    }

    // ================================================================
    // JWT Auth Filter
    // ================================================================

    @Nested
    @DisplayName("JWT Auth Filter")
    class JwtAuthFilterTests {

        @Test
        @DisplayName("deve injetar X-User-Id após validar JWT")
        void deveAdicionarHeaderXUserIdAposValidarJwt() {
            wireMockServer.stubFor(get(urlEqualTo("/api/notes"))
                    .willReturn(okJson("[]")));

            String token = JwtTestHelper.generateToken("user@test.com", "my-user-uuid", "USER");

            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();

            wireMockServer.verify(getRequestedFor(urlEqualTo("/api/notes"))
                    .withHeader("X-User-Id", equalTo("my-user-uuid"))
                    .withHeader("X-User-Email", equalTo("user@test.com"))
                    .withHeader("X-User-Role", equalTo("USER")));
        }

        @Test
        @DisplayName("deve injetar X-User-Role correto")
        void deveInjetarRoleCorreto() {
            wireMockServer.stubFor(get(urlEqualTo("/api/notes"))
                    .willReturn(okJson("[]")));

            String token = JwtTestHelper.generateToken("admin@test.com", "admin-uuid", "ADMIN");

            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();

            wireMockServer.verify(getRequestedFor(urlEqualTo("/api/notes"))
                    .withHeader("X-User-Role", equalTo("ADMIN")));
        }

        @Test
        @DisplayName("deve retornar 401 quando Authorization header está ausente")
        void deveRetornar401SemAuthHeader() {
            webTestClient.get()
                    .uri("/api/notes")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("deve retornar 401 com Bearer vazio")
        void deveRetornar401ComBearerVazio() {
            webTestClient.get()
                    .uri("/api/notes")
                    .header("Authorization", "Bearer ")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("não deve exigir JWT em rotas públicas de auth")
        void naoDeveExigirJwtEmRotasPublicas() {
            wireMockServer.stubFor(post(urlEqualTo("/api/auth/login"))
                    .willReturn(okJson("{\"accessToken\":\"t\"}")));

            webTestClient.post()
                    .uri("/api/auth/login")
                    .header("Content-Type", "application/json")
                    .bodyValue("{\"email\":\"u@t.com\",\"password\":\"p\"}")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    // ================================================================
    // Fallback Controller
    // ================================================================

    @Nested
    @DisplayName("Fallback Controller")
    class FallbackControllerTests {

        @Test
        @DisplayName("fallback de stock deve retornar 503 com mensagem")
        void fallbackDeStockDeveRetornar503() {
            webTestClient.get()
                    .uri("/fallback/stock")
                    .exchange()
                    .expectStatus().isEqualTo(503)
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Stock service indisponível, tente novamente");
        }
    }
}
