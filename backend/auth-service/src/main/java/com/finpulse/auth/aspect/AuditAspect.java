package com.finpulse.auth.aspect;

import com.finpulse.auth.annotation.Audited;
import com.finpulse.auth.entity.AuditLog;
import com.finpulse.auth.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(name = "aop.audit-enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String methodName = sig.getDeclaringType().getSimpleName() + "." + sig.getName();
        String correlationId = MDC.get("correlationId");
        String userEmail = extractUserEmail(sig.getParameterNames(), joinPoint.getArgs());

        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;

        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            success = false;
            errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            saveAuditLog(audited.action(), userEmail, correlationId, methodName, success, errorMessage, duration);
        }
    }

    private void saveAuditLog(String action, String userEmail, String correlationId,
                               String methodName, boolean success, String errorMessage, long duration) {
        try {
            auditLogRepository.save(new AuditLog(
                    action, userEmail, correlationId, methodName, success, errorMessage, duration));
        } catch (Exception ex) {
            // Auditoria não deve impactar a operação principal
            log.warn("[AOP] Falha ao salvar audit log para action={}: {}", action, ex.getMessage());
        }
    }

    private String extractUserEmail(String[] paramNames, Object[] args) {
        if (paramNames == null || args == null) return null;
        for (int i = 0; i < paramNames.length; i++) {
            String name = paramNames[i];
            if (name != null && name.toLowerCase().contains("email")) {
                return args[i] != null ? args[i].toString() : null;
            }
            // Tenta extrair email de objetos request via toString ou reflection
            if (name != null && (name.contains("request") || name.contains("Request"))) {
                try {
                    Object arg = args[i];
                    if (arg != null) {
                        var method = arg.getClass().getMethod("email");
                        Object email = method.invoke(arg);
                        return email != null ? email.toString() : null;
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
