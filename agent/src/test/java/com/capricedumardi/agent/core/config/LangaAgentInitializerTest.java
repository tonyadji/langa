package com.capricedumardi.agent.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * LangaAgentInitializer.premain() itself needs a real java.lang.instrument.Instrumentation
 * from an attached javaagent, so it can't be unit tested here. determineLoggingFramework()
 * is a pure, deterministic private static helper though (the piece that had the
 * LOGGING_FRAMEWORK=none/disabled bug), so it's exercised directly via reflection.
 *
 * Both Logback and Log4j2 are on the test classpath, so "unset" resolves to Logback
 * (checked first in the classpath-detection fallback).
 */
class LangaAgentInitializerTest {

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty("logging.framework");
        ConfigLoader.reloadConfig();
    }

    private String determineLoggingFramework() throws Exception {
        Method method = LangaAgentInitializer.class.getDeclaredMethod("determineLoggingFramework");
        method.setAccessible(true);
        return method.invoke(null).toString();
    }

    private void setLoggingFramework(String value) {
        System.setProperty("logging.framework", value);
        ConfigLoader.reloadConfig();
    }

    @Test
    void explicitNoneDisablesLogCollection() throws Exception {
        setLoggingFramework("none");

        assertEquals("NONE", determineLoggingFramework());
    }

    @Test
    void explicitDisabledDisablesLogCollection() throws Exception {
        setLoggingFramework("disabled");

        assertEquals("NONE", determineLoggingFramework());
    }

    @Test
    void explicitLogbackIsHonoredWhenPresentOnClasspath() throws Exception {
        setLoggingFramework("logback");

        assertEquals("LOGBACK", determineLoggingFramework());
    }

    @Test
    void explicitLog4j2IsHonoredWhenPresentOnClasspath() throws Exception {
        setLoggingFramework("log4j2");

        assertEquals("LOG4J2", determineLoggingFramework());
    }

    @Test
    void explicitLog4jAliasResolvesToLog4j2() throws Exception {
        setLoggingFramework("log4j");

        assertEquals("LOG4J2", determineLoggingFramework());
    }

    @Test
    void unknownValueFallsBackToClasspathDetection() throws Exception {
        setLoggingFramework("not-a-real-framework");

        // Falls back to auto-detection; both frameworks are on the test classpath, and
        // Logback is checked first, so this must NOT be NONE.
        assertEquals("LOGBACK", determineLoggingFramework());
    }

    @Test
    void unsetFallsBackToClasspathDetectionAndFindsLogback() throws Exception {
        System.clearProperty("logging.framework");
        ConfigLoader.reloadConfig();

        assertEquals("LOGBACK", determineLoggingFramework());
    }

    @Test
    void isSpringPresentReturnsTrueWhenASpringClassIsLoadedByAForeignClassLoader() throws Exception {
        Instrumentation instrumentation = mock(Instrumentation.class);
        // A real, already-loaded Spring class works as a stand-in: its classloader is
        // never the (unset, null in tests) AGENT_CLASSLOADER, so it must count as "present".
        when(instrumentation.getAllLoadedClasses())
                .thenReturn(new Class<?>[] {RequestContextHolder.class});

        assertTrue(invokeIsSpringPresent(instrumentation));
    }

    @Test
    void isSpringPresentReturnsFalseWhenNoSpringClassesAreLoaded() throws Exception {
        Instrumentation instrumentation = mock(Instrumentation.class);
        when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[] {String.class});

        assertFalse(invokeIsSpringPresent(instrumentation));
    }

    @Test
    void initSenderAndBuffersWiresUpAnHttpSenderFromConfig() throws Exception {
        String creds = Base64.getEncoder().encodeToString("acc-lga-app".getBytes());
        System.setProperty("langa.ingestion.url", "https://host.example/api/ingestion/http/" + creds);
        System.setProperty("langa.ingestion.secret", "s3cr3t");
        try {
            ConfigLoader.reloadConfig();

            Method method = LangaAgentInitializer.class.getDeclaredMethod("initSenderAndBuffers");
            method.setAccessible(true);
            method.invoke(null);
        } finally {
            System.clearProperty("langa.ingestion.url");
            System.clearProperty("langa.ingestion.secret");
            ConfigLoader.reloadConfig();
        }
    }

    private boolean invokeIsSpringPresent(Instrumentation instrumentation) throws Exception {
        Method method = LangaAgentInitializer.class.getDeclaredMethod("isSpringPresent", Instrumentation.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, instrumentation);
    }
}
