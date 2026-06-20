package com.xyzy.aspect;

import com.alibaba.fastjson.JSON;
import com.xyzy.annotation.SystemLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Aspect
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("@annotation(com.xyzy.annotation.SystemLog)")
    public void pt() {}

    @Around("pt()")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Object ret;
        try {
            handleBefore(joinPoint);
            ret = joinPoint.proceed();
            handleAfter(ret);
        } finally {
            log.info("=======End=======" + System.lineSeparator());
        }
        return ret;
    }

    private void handleBefore(ProceedingJoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        SystemLog systemLog = getSystemLog(joinPoint);
        log.info("=======Start=======");
        log.info("URL           : {}", request.getRequestURL());
        log.info("BusinessName  : {}", systemLog.businessName());
        log.info("HTTP Method   : {}", request.getMethod());
        log.info("Class Method  : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        log.info("IP            : {}", request.getRemoteHost());
        log.info("Request Args  : {}", JSON.toJSONString(joinPoint.getArgs()));
    }

    private SystemLog getSystemLog(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod().getAnnotation(SystemLog.class);
    }

    private void handleAfter(Object ret) {
        log.info("Response      : {}", JSON.toJSONString(ret));
    }
}
