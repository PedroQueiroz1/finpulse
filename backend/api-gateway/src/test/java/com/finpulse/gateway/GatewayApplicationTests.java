package com.finpulse.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gateway - Smoke Test")
class GatewayApplicationTests extends AbstractGatewayTest {

    @Test
    @DisplayName("contexto Spring deve subir corretamente")
    void contextLoads() {
        assertThat(webTestClient).isNotNull();
    }

    @Test
    @DisplayName("actuator/health deve retornar UP")
    void actuatorHealthDeveRetornarUp() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("actuator/gateway/routes deve listar rotas configuradas")
    void actuatorDeveExporRotas() {
        webTestClient.get()
                .uri("/actuator/gateway/routes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }
}
