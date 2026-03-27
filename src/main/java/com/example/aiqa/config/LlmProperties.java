package com.example.aiqa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String baseUrl;
    private String apiKey;
    private String model;
    private String chatPath;
    private Double temperature = 0.2;
    private Integer connectTimeout = 5000;
    private Integer readTimeout = 30000;
}