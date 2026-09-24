package com.capricedumardi.agent.core.aspects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringWebRequestInfoResolverTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void resolveReturnsNullWhenNoRequestAttributesAreBound() {
        assertNull(SpringWebRequestInfoResolver.resolve());
    }

    @Test
    void resolveExtractsUriMethodAndStatusFromBoundRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/widgets");
        when(request.getMethod()).thenReturn("POST");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(204);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        SpringWebRequestInfoResolver.RequestInfo info = SpringWebRequestInfoResolver.resolve();

        assertEquals("/api/widgets", info.uri());
        assertEquals("POST", info.method());
        assertEquals(204, info.status());
    }

    @Test
    void resolveDefaultsStatusToZeroWhenResponseIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/widgets");
        when(request.getMethod()).thenReturn("GET");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, null));

        SpringWebRequestInfoResolver.RequestInfo info = SpringWebRequestInfoResolver.resolve();

        assertEquals(0, info.status());
    }
}
