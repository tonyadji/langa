package com.capricedumardi.agent.core.aspects;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.metrics.Monitored;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.capricedumardi.agent.testsupport.RecordingSenderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AspectJMonitoringAspectTest {

    private RecordingSenderService sender;

    @BeforeEach
    void setUp() {
        AgentTestSupport.ensureBuffersFactoryBootstrapped();
        sender = AgentTestSupport.bootstrapSender();
        sender.reset();
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private ProceedingJoinPoint joinPoint(Object returnValue) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toLongString()).thenReturn("com.foo.Bar.method()");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn(returnValue);
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
        AspectJMonitoringAspect aspect = new AspectJMonitoringAspect();

        Object result = aspect.aroundMonitoredMethod(joinPoint("ok"), monitored("op"));
        BuffersFactory.getMetricBufferInstance().flush();

        assertEquals("ok", result);
        MetricRequestDto dto = (MetricRequestDto) sender.sentPayloads().get(0);
        var metric = dto.entries().get(0);
        assertEquals("op", metric.getName());
        assertEquals("SUCCESS", metric.getStatus());
        assertEquals(0, metric.getHttpStatus());
    }

    @Test
    void tracksHttpContextWhenRequestAttributesAreBound() throws Throwable {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/things");
        when(request.getMethod()).thenReturn("GET");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        AspectJMonitoringAspect aspect = new AspectJMonitoringAspect();
        aspect.aroundMonitoredMethod(joinPoint("ok"), monitored("op"));
        BuffersFactory.getMetricBufferInstance().flush();

        MetricRequestDto dto = (MetricRequestDto) sender.sentPayloads().get(0);
        var metric = dto.entries().get(0);
        assertEquals("/api/things", metric.getUri());
        assertEquals("GET", metric.getHttpMethod());
        assertEquals(200, metric.getHttpStatus());
    }

    @Test
    void tracksErrorStatusAndRethrowsWhenJoinPointThrows() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toLongString()).thenReturn("com.foo.Bar.method()");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        AspectJMonitoringAspect aspect = new AspectJMonitoringAspect();

        assertThrows(IllegalStateException.class, () -> aspect.aroundMonitoredMethod(joinPoint, monitored("op")));
        BuffersFactory.getMetricBufferInstance().flush();

        MetricRequestDto dto = (MetricRequestDto) sender.sentPayloads().get(0);
        assertEquals("ERROR", dto.entries().get(0).getStatus());
    }
}
