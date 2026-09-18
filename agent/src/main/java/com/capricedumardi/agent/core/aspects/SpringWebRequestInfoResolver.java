package com.capricedumardi.agent.core.aspects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Isolates every reference to Spring-Web/Servlet types behind a class that is only
 * loaded (and only needs those types resolvable) when {@link #resolve()} is actually
 * called. Callers must gate that call behind a classpath check so this class is never
 * linked in apps that don't have spring-web/servlet-api on their classpath.
 */
final class SpringWebRequestInfoResolver {

    private SpringWebRequestInfoResolver() {
    }

    static RequestInfo resolve() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        int responseStatus = Optional.ofNullable(attributes.getResponse())
                .map(HttpServletResponse::getStatus)
                .orElse(0);

        return new RequestInfo(request.getRequestURI(), request.getMethod(), responseStatus);
    }

    record RequestInfo(String uri, String method, int status) {
    }
}
