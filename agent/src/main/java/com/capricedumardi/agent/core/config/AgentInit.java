package com.capricedumardi.agent.core.config;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.helpers.EnvironmentUtils;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.services.SenderService;
import com.capricedumardi.agent.core.services.SenderServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.weaver.loadtime.Agent;

import java.lang.instrument.Instrumentation;

public class AgentInit {

    private static final Logger log = LogManager.getLogger(AgentInit.class);
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_FLUSH_DELAY_IN_SECONDS = 5;

    private AgentInit() {}

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("== Langa Agent Init ==");

        try {
            initSenderAndBuffers();
        } catch (Exception e) {
            log.error("FATAL: Could not initialize sender/buffers. Agent logging disabled.", e);
            return;
        }

        if (isSpringPresent()) {
            AppenderBinding.withLogBackAppender().bind();
            log.info("Spring detected → expecting @EnableAspectJAutoProxy");
        } else {
            AppenderBinding.withLog4jAppender().bind();
            log.info("Spring NOT detected → using AspectJ weaver");
            try {
                Agent.premain(agentArgs, inst);
            } catch (Exception e) {
                log.error("Unable to init agent : {}", e.getMessage(), e);
                e.printStackTrace();
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.debug("ShutDownHook Received : Terminating Langa Agent");
            BuffersFactory.shutdownAll();
        }));
    }

    private static void initSenderAndBuffers() {
        IngestionParamsResolver resolver = EnvironmentUtils.getIngestionParamsResolver();
        SenderService senderService = SenderServiceFactory.create(resolver);
        BuffersFactory.init(senderService, resolver.resolveAppKey(), resolver.resolveAccountKey(),
                DEFAULT_BATCH_SIZE, DEFAULT_FLUSH_DELAY_IN_SECONDS);
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
