package com.finpulse.stock.resilience;

import com.finpulse.stock.AbstractIntegrationTest;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Time Limiter - Configuração")
class TimeLimiterTest extends AbstractIntegrationTest {

    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;

    @Test
    @DisplayName("deve configurar timeout de 3s para chamadas externas")
    void deveTimeoutChamadaMaisLongaQue3s() {
        TimeLimiter tl = timeLimiterRegistry.timeLimiter("external-apis");

        assertThat(tl.getTimeLimiterConfig().getTimeoutDuration())
                .isEqualTo(Duration.ofSeconds(3));
    }
}
