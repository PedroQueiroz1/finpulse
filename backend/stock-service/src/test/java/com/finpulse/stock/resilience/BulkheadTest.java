package com.finpulse.stock.resilience;

import com.finpulse.stock.AbstractIntegrationTest;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Bulkhead - Testes de Isolamento")
class BulkheadTest extends AbstractIntegrationTest {

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Test
    @DisplayName("deve isolar threads entre endpoints (bulkhead external-apis configurado)")
    void deveIsolarThreadsEntreEndpoints() {
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("external-apis");

        assertThat(bulkhead).isNotNull();
        assertThat(bulkhead.getBulkheadConfig().getMaxConcurrentCalls()).isEqualTo(20);

        // Bulkhead disponível — deve permitir chamadas dentro do limite
        assertThat(bulkhead.tryAcquirePermission()).isTrue();
        bulkhead.releasePermission();
    }

    @Test
    @DisplayName("deve rejeitar chamada quando bulkhead está cheio")
    void deveRejeitarQuandoBulkheadCheio() {
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("external-apis");
        int max = bulkhead.getBulkheadConfig().getMaxConcurrentCalls();

        // Ocupa todos os slots
        for (int i = 0; i < max; i++) {
            assertThat(bulkhead.tryAcquirePermission()).isTrue();
        }

        try {
            // 21ª chamada deve ser rejeitada
            assertThat(bulkhead.tryAcquirePermission()).isFalse();
        } finally {
            for (int i = 0; i < max; i++) {
                bulkhead.releasePermission();
            }
        }
    }
}
