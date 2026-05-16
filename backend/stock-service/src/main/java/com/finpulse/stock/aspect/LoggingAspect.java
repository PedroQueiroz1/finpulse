package com.finpulse.stock.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@ConditionalOnProperty(name = "aop.logging-enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    private static final String[] SENSITIVE_PARAMS = {"password", "senha", "token", "secret", "key", "apikey", "credential"};

    @Around("execution(* com.finpulse.stock.controller.*.*(..)) || execution(* com.finpulse.stock.service.*.*(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String method = sig.getDeclaringType().getSimpleName() + "." + sig.getName();
        Object[] sanitized = sanitize(sig.getParameterNames(), joinPoint.getArgs());

        log.info("[AOP] >> {} args={}", method, Arrays.toString(sanitized));
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("[AOP] << {} OK em {}ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("[AOP] << {} ERRO em {}ms: {}", method, System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        }
    }

    private Object[] sanitize(String[] names, Object[] args) {
        if (names == null || args == null) return args;
        Object[] result = Arrays.copyOf(args, args.length);
        for (int i = 0; i < names.length; i++) {
            if (isSensitive(names[i])) {
                result[i] = "***";
            }
        }
        return result;
    }

    private boolean isSensitive(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        for (String s : SENSITIVE_PARAMS) {
            if (lower.contains(s)) return true;
        }
        return false;
    }
}
