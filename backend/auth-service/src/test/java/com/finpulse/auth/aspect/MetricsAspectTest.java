package com.finpulse.auth.aspect;

import com.finpulse.auth.AbstractIntegrationTest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.repository.AuditLogRepository;
import com.finpulse.auth.repository.UserRepository;
import com.finpulse.auth.service.AuthService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MetricsAspect - Testes")
class MetricsAspectTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("deve registrar timer após chamada ao serviço")
    void deveRegistrarTimer() {
        authService.register(new RegisterRequest("Metrics", "metrics@test.com", "senha123"));

        Timer timer = meterRegistry.find("method.duration")
                .tag("class", "AuthService")
                .tag("method", "register")
                .tag("status", "success")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deve incrementar counter após chamada ao serviço")
    void deveIncrementarCounter() {
        authService.register(new RegisterRequest("Counter", "counter@test.com", "senha123"));

        Counter counter = meterRegistry.find("method.calls.total")
                .tag("class", "AuthService")
                .tag("method", "register")
                .tag("status", "success")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    @DisplayName("deve criar métrica com nome do método correto")
    void deveCriarMetricaComNomeDoMetodo() {
        authService.register(new RegisterRequest("NameTest", "name@test.com", "senha123"));

        assertThat(meterRegistry.find("method.duration")
                .tag("method", "register")
                .timers()).isNotEmpty();
    }
}
