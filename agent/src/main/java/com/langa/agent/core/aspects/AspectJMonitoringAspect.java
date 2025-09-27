package com.langa.agent.core.aspects;

import com.langa.agent.core.metrics.DefaultMetricsCollector;
import com.langa.agent.core.metrics.MetricsCollector;
import com.langa.agent.core.metrics.Monitored;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

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
            long duration = System.currentTimeMillis() - start;
            collector.track(joinPoint.getSignature().toShortString(), duration, status);
        }
    }
}
