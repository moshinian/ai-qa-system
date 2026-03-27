package com.example.aiqa.client;

import com.example.aiqa.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public LlmClient(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(llmProperties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(llmProperties.getReadTimeout()))
                .build();
    }

    public String chat(String prompt) {
        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            return "当前未配置 LLM_API_KEY，暂时返回模拟答案。\n\nPrompt:\n" + prompt;
        }

        String url = llmProperties.getBaseUrl() + llmProperties.getChatPath();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(llmProperties.getApiKey());

        Map<String, Object> body = Map.of(
                "model", llmProperties.getModel(),
                "temperature", llmProperties.getTemperature(),
                "messages", List.of(
                        Map.of("role", "system", "content", "你是一个严谨的知识库问答助手。"),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return "大模型调用失败，HTTP状态码: " + response.getStatusCode();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return "大模型返回为空";
            }

            return contentNode.asText();

        } catch (Exception e) {
            return "调用大模型异常: " + e.getMessage();
        }
    }
}