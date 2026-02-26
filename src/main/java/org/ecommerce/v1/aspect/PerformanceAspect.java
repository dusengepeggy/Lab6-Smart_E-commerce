package org.ecommerce.v1.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ecommerce.v1.metrics.EndpointMetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    private final EndpointMetricsStore endpointMetricsStore;

    public PerformanceAspect(EndpointMetricsStore endpointMetricsStore) {
        this.endpointMetricsStore = endpointMetricsStore;
    }

    @Around("execution(* org.ecommerce.v1.controller..*(..)) || execution(* org.ecommerce.v1.service..*(..))")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        endpointMetricsStore.record(target, elapsed);
        if (elapsed > 500) {
            log.warn("Slow call [{}ms]: {}", elapsed, target);
        } else {
            log.info("{} took {}ms", target, elapsed);
        }
        return result;
    }
}
