package com.capricedumardi.agent.core.aspects;

import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.metrics.DefaultMetricsCollector;
import com.capricedumardi.agent.core.metrics.MetricsCollector;
import com.capricedumardi.agent.core.metrics.Monitored;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Used on the AspectJ load-time-weaving path (see LangaAgentInitializer), which targets
 * apps regardless of whether they use Spring or expose HTTP endpoints at all. HTTP request
 * enrichment is therefore best-effort: it's only attempted when spring-web/servlet-api are
 * actually present on the host classpath, and any failure falls back to tracking without it
 * instead of breaking the monitored method call.
 */
@Aspect
public class AspectJMonitoringAspect {
    private static final boolean SPRING_WEB_AVAILABLE = isSpringWebAvailable();

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
            track(joinPoint, monitored, duration, status);
        }
    }

    private void track(ProceedingJoinPoint joinPoint, Monitored monitored, long duration, String status) {
        String signature = joinPoint.getSignature().toLongString();

        if (SPRING_WEB_AVAILABLE) {
            try {
                SpringWebRequestInfoResolver.RequestInfo info = SpringWebRequestInfoResolver.resolve();
                if (info != null) {
                    collector.track(monitored.name(), signature, duration, status,
                            info.uri(), info.method(), info.status());
                    return;
                }
            } catch (Throwable t) {
                LangaPrinter.printError("AspectJMonitoringAspect: failed to resolve HTTP request context: " + t.getMessage());
            }
        }

        collector.track(monitored.name(), signature, duration, status, null, null, 0);
    }

    private static boolean isSpringWebAvailable() {
        try {
            Class.forName("org.springframework.web.context.request.RequestContextHolder");
            Class.forName("jakarta.servlet.http.HttpServletRequest");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
