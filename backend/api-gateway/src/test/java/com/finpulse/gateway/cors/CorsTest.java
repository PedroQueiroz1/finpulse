package com.finpulse.gateway.cors;

import com.finpulse.gateway.AbstractGatewayTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@DisplayName("Gateway - CORS")
class CorsTest extends AbstractGatewayTest {

    @Test
    @DisplayName("deve permitir requisição CORS de origin configurada")
    void devePermitirRequestsCorsConfigurados() {
        wireMockServer.stubFor(get(urlEqualTo("/api/auth/health"))
                .willReturn(okJson("{\"status\":\"UP\"}")));

        webTestClient.options()
                .uri("/api/auth/health")
                .header(HttpHeaders.ORIGIN, "http://allowed.test.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .exchange()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    @Test
    @DisplayName("não deve incluir CORS headers para origin não configurada")
    void deveRejeitarOriginsNaoPermitidas() {
        webTestClient.options()
                .uri("/api/auth/health")
                .header(HttpHeaders.ORIGIN, "http://evil-origin.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                .exchange()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }
}
