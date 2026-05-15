package com.finpulse.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WireMock + Testcontainers Setup")
class WireMockSetupTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("container Redis deve estar rodando")
    void redisDeveEstarRodando() {
        assertThat(redis.isRunning()).isTrue();
    }

    @Test
    @DisplayName("servidor WireMock deve estar rodando")
    void wireMockDeveEstarRodando() {
        assertThat(wireMock.isRunning()).isTrue();
    }

    @Test
    @DisplayName("porta do WireMock deve ser positiva")
    void wireMockDeveExporUmaPorta() {
        assertThat(wireMock.port()).isPositive();
    }
}
