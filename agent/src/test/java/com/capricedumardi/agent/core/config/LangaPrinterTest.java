package com.capricedumardi.agent.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangaPrinterTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void captureStreams() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void printErrorAlwaysWritesToStderr() {
        LangaPrinter.printError("boom");

        assertTrue(err.toString().contains("boom"));
        assertTrue(out.toString().isEmpty());
    }

    @Test
    void printWarningAlwaysWritesToStdout() {
        LangaPrinter.printWarning("careful");

        assertTrue(out.toString().contains("careful"));
    }

    @Test
    void agentStartingWritesBanner() {
        LangaPrinter.agentStarting();

        assertTrue(out.toString().contains("Langa Agent Starting"));
    }

    @Test
    void agentInitializationCompleteWritesBanner() {
        LangaPrinter.agentInitializationComplete();

        assertTrue(out.toString().contains("Initialization Complete"));
    }

    @Test
    void printTraceRespectsCurrentDebugFlag() {
        boolean debugEnabled = LangaPrinter.isDebugEnabled();

        LangaPrinter.printTrace("trace-message");

        if (debugEnabled) {
            assertTrue(out.toString().contains("trace-message"));
        } else {
            assertTrue(out.toString().isEmpty());
        }
    }

    @Test
    void printConditionalErrorRespectsCurrentDebugFlag() {
        boolean debugEnabled = LangaPrinter.isDebugEnabled();

        LangaPrinter.printConditionalError("conditional-message");

        if (debugEnabled) {
            assertTrue(err.toString().contains("conditional-message"));
        } else {
            assertFalse(err.toString().contains("conditional-message"));
        }
    }
}
