package com.capricedumardi.agent.core.config;

public class LangaPrinter {

    private static AgentConfig agentConfig;

    public static boolean isDebugEnabled() {
        if (agentConfig == null) agentConfig = ConfigLoader.getConfigInstance();
        return agentConfig.isDebugMode();
    }

    public static void printError(String message) {
        System.err.println("===== LANGA AGENT ERROR =======");
        System.err.println(message);
        System.err.println("===============================");
    }

    public static void printConditionalError(String s) {
        if (isDebugEnabled()) {
            printError(s);
        }
    }

    public static void printWarning(String message) {
        System.out.println("===== LANGA AGENT WANRING =====");
        System.out.println(message);
        System.out.println("===============================");
    }

    public static void printTrace(String message) {
        if (isDebugEnabled()) {
            System.out.println("===== LANGA AGENT DEBUG =======");
            System.out.println(message);
            System.out.println("===============================");
        }
    }

    public static void agentStarting() {
        System.out.println("========================================");
        System.out.println("  Langa Agent Starting");
        System.out.println("========================================");
    }


}
