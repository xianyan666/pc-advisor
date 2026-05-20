package com.pcadvisor.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 日志切面（记录接口请求日志）
 */
@Aspect // 标识为切面类
@Component // 交给Spring容器管理
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 切入点：匹配controller包下所有方法
     */
    @Pointcut("execution(* com.pcadvisor.controller..*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录请求入参、出参、耗时
     */
    @Around("controllerPointcut()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        // 2. 记录请求开始信息
        log.info("===== 请求开始 =====");
        log.info("请求时间：{}", LocalDateTime.now());
        log.info("请求URL：{}", url);
        log.info("请求方式：{}", method);
        log.info("请求IP：{}", ip);
        log.info("请求方法：{}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("请求参数：{}", joinPoint.getArgs());

        // 3. 执行目标方法
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed(); // 执行原方法
        long endTime = System.currentTimeMillis();

        // 4. 记录响应信息
        log.info("响应结果：{}", result);
        log.info("请求耗时：{}ms", endTime - startTime);
        log.info("===== 请求结束 =====\n");

        return result;
    }

}