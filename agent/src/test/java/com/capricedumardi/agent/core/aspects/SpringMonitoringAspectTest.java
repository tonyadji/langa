package com.capricedumardi.agent.core.aspects;

import com.capricedumardi.agent.core.metrics.MetricsCollector;
import com.capricedumardi.agent.core.metrics.Monitored;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringMonitoringAspectTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private ProceedingJoinPoint joinPoint(String longSignature) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toLongString()).thenReturn(longSignature);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn("ok");
        return joinPoint;
    }

    private Monitored monitored(String name) {
        return new Monitored() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return Monitored.class;
            }
        };
    }

    @Test
    void tracksSuccessWithoutHttpContext() throws Throwable {
        MetricsCollector collector = mock(MetricsCollector.class);
        SpringMonitoringAspect aspect = new SpringMonitoringAspect(collector);
        ProceedingJoinPoint joinPoint = joinPoint("com.foo.Bar.method()");

        Object result = aspect.aroundMonitoredMethod(joinPoint, monitored("op"));

        assertEquals("ok", result);
        ArgumentCaptor<Long> duration = ArgumentCaptor.forClass(Long.class);
        verify(collector).track(eq("op"), eq("com.foo.Bar.method()"),
                duration.capture(), eq("SUCCESS"), isNull(), isNull(), eq(0));
        assertTrue(duration.getValue() >= 0);
    }

    @Test
    void tracksHttpContextWhenRequestAttributesArePresent() throws Throwable {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/things");
        when(request.getMethod()).thenReturn("GET");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(201);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        MetricsCollector collector = mock(MetricsCollector.class);
        SpringMonitoringAspect aspect = new SpringMonitoringAspect(collector);
        ProceedingJoinPoint joinPoint = joinPoint("com.foo.Bar.method()");

        aspect.aroundMonitoredMethod(joinPoint, monitored("op"));

        verify(collector).track(eq("op"), eq("com.foo.Bar.method()"), anyLong(), eq("SUCCESS"),
                eq("/api/things"), eq("GET"), eq(201));
    }

    @Test
    void tracksErrorStatusAndRethrowsWhenJoinPointThrows() throws Throwable {
        MetricsCollector collector = mock(MetricsCollector.class);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toLongString()).thenReturn("com.foo.Bar.method()");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        SpringMonitoringAspect aspect = new SpringMonitoringAspect(collector);

        assertThrows(IllegalStateException.class, () -> aspect.aroundMonitoredMethod(joinPoint, monitored("op")));
        verify(collector).track(eq("op"), eq("com.foo.Bar.method()"), anyLong(), eq("ERROR"), isNull(), isNull(), eq(0));
    }
}
