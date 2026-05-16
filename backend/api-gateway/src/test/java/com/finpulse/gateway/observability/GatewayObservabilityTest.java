package com.finpulse.gateway.observability;

import com.finpulse.gateway.AbstractGatewayTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gateway - Observabilidade")
class GatewayObservabilityTest extends AbstractGatewayTest {

    @Test
    @DisplayName("deve exportar rotas configuradas no actuator")
    void deveExporRotasNoActuator() {
        webTestClient.get()
                .uri("/actuator/gateway/routes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].route_id").exists();
    }

    @Test
    @DisplayName("deve exportar métricas por rota no endpoint prometheus")
    void deveExporMetricasPorRota() {
        webTestClient.get()
                .uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .isNotBlank()
                        .contains("jvm_"));
    }
}
