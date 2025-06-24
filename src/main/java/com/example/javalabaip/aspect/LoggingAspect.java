package com.example.javalabaip.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.javalabaip.controller..*.*(..))")
    public void logControllerMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        logger.info("Executing {}.{} with arguments: {}", className, methodName, args);
    }

    @Before("execution(* com.example.javalabaip.service..*.*(..))")
    public void logServiceMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        logger.debug("Executing service method {}.{} with arguments: {}", className, methodName, args);
    }

    @AfterThrowing(pointcut = "execution(* com.example.javalabaip..*.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.error("Exception in {}.{}: {}", className, methodName, ex.getMessage(), ex);
    }
}