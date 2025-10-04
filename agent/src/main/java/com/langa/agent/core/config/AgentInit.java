package com.langa.agent.core.config;

import java.lang.instrument.Instrumentation;

public class AgentInit {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("== MonitoringAgent loaded ==");

        if (isSpringPresent()) {
            System.out.println("Spring detected → expecting @EnableAspectJAutoProxy");
        } else {
            System.out.println("Spring NOT detected → using AspectJ weaver");
            try {
                //TODO : fix this
                //org.aspectj.weaver.loadtime.Agent.premain(agentArgs, inst);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isSpringPresent() {
        try {
            Class.forName("org.springframework.aop.framework.ProxyFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
