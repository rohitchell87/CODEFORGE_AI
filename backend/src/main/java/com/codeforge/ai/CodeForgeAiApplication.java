package com.codeforge.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@Slf4j
public class CodeForgeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeForgeAiApplication.class, args);
        log.info("BACKEND STARTED SUCCESSFULLY on http://localhost:8080");
    }

    @Bean
    public ApplicationRunner logMappings(RequestMappingHandlerMapping mapping) {
        return args -> {
            try {
                mapping.getHandlerMethods().forEach((info, method) -> {
                    String patterns = "";
                    if (info.getPatternsCondition() != null) {
                        patterns = String.join(",", info.getPatternsCondition().getPatterns());
                    } else if (info.getPathPatternsCondition() != null) {
                        patterns = String.join(",", info.getPathPatternsCondition().getPatternValues());
                    }
                    log.info("Mapped {} -> {}#{}", patterns, method.getBeanType().getSimpleName(), method.getMethod().getName());
                });
            } catch (Exception e) {
                log.warn("Could not log request mappings", e);
            }
        };
    }
}
