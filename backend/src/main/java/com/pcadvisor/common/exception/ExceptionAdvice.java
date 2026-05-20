package com.pcadvisor.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 异常通知切面（用于监控和记录异常）
 */
@Slf4j
@Aspect
@Component
public class ExceptionAdvice {

    /**
     * 切入点：所有Service方法
     */
    @Pointcut("execution(* com.pcadvisor.service..*.*(..))")
    public void servicePointcut() {}

    /**
     * 切入点：所有Controller方法
     */
    @Pointcut("execution(* com.pcadvisor.controller..*.*(..))")
    public void controllerPointcut() {}

    /**
     * 环绕通知：监控Service层异常
     */
    @Around("servicePointcut()")
    public Object monitorService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录慢查询
            if (executionTime > 1000) {
                log.warn("Service方法执行缓慢: {}，耗时: {}ms", fullMethodName, executionTime);
            }

            return result;
        } catch (BusinessException e) {
            // 业务异常记录警告日志
            log.warn("Service业务异常[{}]: {}", fullMethodName, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 其他异常记录错误日志
            log.error("Service系统异常[{}]: {}", fullMethodName, e.getMessage(), e);
            throw ExceptionUtils.wrapAsBusinessException(e);
        }
    }

    /**
     * 环绕通知：监控Controller层异常
     */
    @Around("controllerPointcut()")
    public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录慢请求
            if (executionTime > 2000) {
                log.warn("Controller方法执行缓慢: {}，耗时: {}ms", fullMethodName, executionTime);
            }

            return result;
        } catch (BusinessException e) {
            // 业务异常不记录错误日志（已在GlobalExceptionHandler处理）
            throw e;
        } catch (Exception e) {
            log.error("Controller系统异常[{}]: {}", fullMethodName, e.getMessage(), e);
            throw ExceptionUtils.wrapAsBusinessException(e);
        }
    }
}