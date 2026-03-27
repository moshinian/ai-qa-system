package com.example.aiqa;

import com.example.aiqa.config.LlmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LlmProperties.class)
public class AiQaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiQaApplication.class, args);
    }
}