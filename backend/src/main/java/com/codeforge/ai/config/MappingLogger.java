package com.codeforge.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

@Component
public class MappingLogger {

    private static final Logger log = LoggerFactory.getLogger(MappingLogger.class);

    private final RequestMappingHandlerMapping mapping;

    public MappingLogger(RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logAllMappings() {
        log.info("Listing all request mappings registered by Spring:");
        mapping.getHandlerMethods().forEach((info, method) -> {
            try {
                Set<String> patterns = info.getPatternValues();
                patterns.forEach(p -> log.info("Mapped {} -> {}", p, method.getMethod()));
            } catch (Exception ex) {
                log.warn("Failed to read mapping patterns for {}: {}", method.getMethod(), ex.getMessage());
            }
        });
    }
}
