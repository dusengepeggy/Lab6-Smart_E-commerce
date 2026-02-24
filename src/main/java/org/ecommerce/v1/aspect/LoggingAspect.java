package org.ecommerce.v1.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* org.ecommerce.v1.controller..*(..)) || execution(* org.ecommerce.v1.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        log.debug("Entering: {}", target);
        Object result = joinPoint.proceed();
        log.debug("Exiting: {}", target);
        return result;
    }
}
