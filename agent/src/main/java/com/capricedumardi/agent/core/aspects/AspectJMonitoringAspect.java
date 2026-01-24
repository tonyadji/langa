package com.capricedumardi.agent.core.aspects;

import com.capricedumardi.agent.core.metrics.DefaultMetricsCollector;
import com.capricedumardi.agent.core.metrics.MetricsCollector;
import com.capricedumardi.agent.core.metrics.Monitored;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Aspect
public class AspectJMonitoringAspect {
    private final MetricsCollector collector = new DefaultMetricsCollector();

    @Around("@annotation(monitored)")
    public Object aroundMonitoredMethod(ProceedingJoinPoint joinPoint, Monitored monitored) throws Throwable {
        long start = System.currentTimeMillis();
        String status = "SUCCESS";
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            status = "ERROR";
            throw t;
        } finally {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            long duration = System.currentTimeMillis() - start;
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                int responseStatus = Optional.ofNullable(attributes.getResponse())
                        .map(HttpServletResponse::getStatus)
                        .orElse(0);
                collector.track(monitored.name(), joinPoint.getSignature().toLongString(), duration, status, request.getRequestURI(), request.getMethod(), responseStatus);
            } else {
                collector.track(monitored.name(), joinPoint.getSignature().toLongString(), duration, status, null, null, 0);
            }

        }
    }
}
