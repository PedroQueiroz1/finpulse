package com.finpulse.auth.aspect;

import com.finpulse.auth.AbstractIntegrationTest;
import com.finpulse.auth.dto.LoginRequest;
import com.finpulse.auth.dto.RegisterRequest;
import com.finpulse.auth.entity.AuditLog;
import com.finpulse.auth.repository.AuditLogRepository;
import com.finpulse.auth.repository.UserRepository;
import com.finpulse.auth.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditAspect - Testes")
class AuditAspectTest extends AbstractIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("deve salvar audit log quando método tem @Audited")
    void deveSalvarAuditoriaParaRegistro() {
        authService.register(new RegisterRequest("Audit", "audit@test.com", "senha123"));

        List<AuditLog> logs = auditLogRepository.findByActionOrderByCreatedAtDesc("USER_REGISTER");
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo("USER_REGISTER");
        assertThat(logs.get(0).isSuccess()).isTrue();
        assertThat(logs.get(0).getMethodName()).contains("register");
    }

    @Test
    @DisplayName("deve salvar audit log com email do usuário")
    void deveSalvarEmailDoUsuario() {
        authService.register(new RegisterRequest("EmailAudit", "email-audit@test.com", "senha123"));

        List<AuditLog> logs = auditLogRepository.findByUserEmailOrderByCreatedAtDesc("email-audit@test.com");
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getUserEmail()).isEqualTo("email-audit@test.com");
    }

    @Test
    @DisplayName("deve salvar audit log mesmo quando método falha")
    void deveSalvarAuditoriaQuandoMetodoFalha() {
        authService.register(new RegisterRequest("FailUser", "fail@test.com", "senha123"));
        auditLogRepository.deleteAll();

        assertThatThrownBy(() -> authService.login(new LoginRequest("fail@test.com", "senha-errada")));

        List<AuditLog> logs = auditLogRepository.findByActionOrderByCreatedAtDesc("USER_LOGIN");
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).isSuccess()).isFalse();
        assertThat(logs.get(0).getErrorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("deve salvar audit log para login bem-sucedido")
    void deveSalvarAuditoriaParaLogin() {
        authService.register(new RegisterRequest("LoginAudit", "login-audit@test.com", "senha123"));
        auditLogRepository.deleteAll();

        authService.login(new LoginRequest("login-audit@test.com", "senha123"));

        List<AuditLog> logs = auditLogRepository.findByActionOrderByCreatedAtDesc("USER_LOGIN");
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).isSuccess()).isTrue();
        assertThat(logs.get(0).getDurationMs()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("não deve salvar senha na auditoria")
    void naoDeveSalvarSenhaNaAuditoria() {
        authService.register(new RegisterRequest("SenhaTest", "senha-audit@test.com", "minha-senha-secreta"));

        List<AuditLog> logs = auditLogRepository.findByActionOrderByCreatedAtDesc("USER_REGISTER");
        assertThat(logs).hasSize(1);
        // Nenhum campo do audit log deve conter a senha em texto claro
        AuditLog log = logs.get(0);
        String allFields = String.join(" ",
                log.getAction(), String.valueOf(log.getUserEmail()),
                log.getMethodName());
        assertThat(allFields).doesNotContain("minha-senha-secreta");
    }
}
