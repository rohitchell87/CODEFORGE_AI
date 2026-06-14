package com.codeforge.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class CodeForgeAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeForgeAiApplication.class, args);
        log.info("BACKEND STARTED SUCCESSFULLY on http://localhost:8080");
    }
}
