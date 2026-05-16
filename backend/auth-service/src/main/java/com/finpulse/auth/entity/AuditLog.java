package com.finpulse.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "method_name", nullable = false, length = 150)
    private String methodName;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(String action, String userEmail, String correlationId,
                    String methodName, boolean success, String errorMessage, Long durationMs) {
        this.action = action;
        this.userEmail = userEmail;
        this.correlationId = correlationId;
        this.methodName = methodName;
        this.success = success;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getUserEmail() { return userEmail; }
    public String getCorrelationId() { return correlationId; }
    public String getMethodName() { return methodName; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public Long getDurationMs() { return durationMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
