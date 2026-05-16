package com.finpulse.auth.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(name = "aop.metrics-enabled", havingValue = "true", matchIfMissing = true)
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.finpulse.auth.controller.*.*(..)) || execution(* com.finpulse.auth.service.*.*(..))")
    public Object recordMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getName();

        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "success";
        try {
            return joinPoint.proceed();
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            sample.stop(Timer.builder("method.duration")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", status)
                    .register(meterRegistry));
            meterRegistry.counter("method.calls.total",
                    "class", className,
                    "method", methodName,
                    "status", status).increment();
        }
    }
}
