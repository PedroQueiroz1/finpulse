package com.finpulse.auth.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.finpulse.auth.AbstractIntegrationTest;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.repository.AuditLogRepository;
import com.finpulse.auth.repository.UserRepository;
import com.finpulse.auth.service.AuthService;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoggingAspect - Testes")
class LoggingAspectTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger aspectLogger;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        aspectLogger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        aspectLogger.addAppender(listAppender);
        aspectLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void tearDown() {
        aspectLogger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("deve logar entrada do método com args")
    void deveLogarEntradaDoMetodo() {
        authService.register(new RegisterRequest("Test", "log@test.com", "senha123"));

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(e -> e.getFormattedMessage().contains("[AOP] >>")
                && e.getFormattedMessage().contains("register"));
    }

    @Test
    @DisplayName("deve logar saída do método com tempo de execução")
    void deveLogarSaidaComTempo() {
        authService.register(new RegisterRequest("Test2", "log2@test.com", "senha123"));

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(e -> e.getFormattedMessage().contains("[AOP] <<")
                && e.getFormattedMessage().contains("OK em")
                && e.getFormattedMessage().contains("ms"));
    }

    @Test
    @DisplayName("deve logar exceção quando método falha")
    void deveLogarExcecao() {
        authService.register(new RegisterRequest("Test3", "err@test.com", "senha123"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("err@test.com", "senha-errada")));

        List<ILoggingEvent> errorLogs = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.ERROR)
                .toList();
        assertThat(errorLogs).anyMatch(e -> e.getFormattedMessage().contains("[AOP] <<")
                && e.getFormattedMessage().contains("ERRO"));
    }

    @Test
    @DisplayName("não deve logar senha em texto claro nos parâmetros")
    void naoDeveLogarSenha() {
        authService.register(new RegisterRequest("Test4", "safe@test.com", "minha-senha-secreta"));

        List<ILoggingEvent> logs = listAppender.list;
        // Verifica que "minha-senha-secreta" não aparece em nenhum log
        assertThat(logs).noneMatch(e -> e.getFormattedMessage().contains("minha-senha-secreta"));
        // Mas deve aparecer "***" no lugar
        assertThat(logs).anyMatch(e -> e.getFormattedMessage().contains("***"));
    }
}
