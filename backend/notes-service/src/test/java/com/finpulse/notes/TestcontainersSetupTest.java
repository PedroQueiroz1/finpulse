package com.finpulse.notes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testcontainers Setup")
class TestcontainersSetupTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("container MongoDB deve estar rodando")
    void containerDeveEstarRodando() {
        assertThat(mongoDb.isRunning()).isTrue();
    }

    @Test
    @DisplayName("URL do MongoDB deve começar com mongodb://")
    void urlDeveSerMongodb() {
        assertThat(mongoDb.getReplicaSetUrl()).startsWith("mongodb://");
    }
}
