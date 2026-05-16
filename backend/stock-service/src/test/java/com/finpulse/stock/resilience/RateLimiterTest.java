package com.finpulse.stock.resilience;

import com.finpulse.stock.AbstractIntegrationTest;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Rate Limiter - Configuração")
class RateLimiterTest extends AbstractIntegrationTest {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Test
    @DisplayName("deve limitar a 100 chamadas por segundo para APIs externas")
    void deveRejeitarChamadasAcimaDoLimite() {
        RateLimiter rl = rateLimiterRegistry.rateLimiter("external-apis");

        assertThat(rl.getRateLimiterConfig().getLimitForPeriod()).isEqualTo(100);
        assertThat(rl.getRateLimiterConfig().getLimitRefreshPeriod())
                .isEqualTo(Duration.ofSeconds(1));
    }
}
