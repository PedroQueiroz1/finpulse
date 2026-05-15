package com.finpulse.auth;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TestcontainersSetupTest extends AbstractIntegrationTest {

    @Test
    void containerDeveEstarRodando() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getJdbcUrl()).startsWith("jdbc:postgresql://");
    }
}