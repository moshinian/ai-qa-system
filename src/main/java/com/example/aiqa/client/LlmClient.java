package com.example.aiqa.client;

import com.example.aiqa.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);
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
            return "当前未配置 DEEPSEEK_API_KEY，暂时返回模拟答案。\n\nPrompt:\n" + prompt;
        }

        String url = llmProperties.getBaseUrl() + llmProperties.getChatPath();
        log.info("调用大模型，请求地址: {}, model: {}", url, llmProperties.getModel());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(llmProperties.getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", llmProperties.getModel());
        body.put("messages", List.of(
                Map.of("role", "system", "content", llmProperties.getSystemPrompt()),
                Map.of("role", "user", "content", prompt)
        ));
        body.put("stream", llmProperties.getStream());
        body.put("temperature", llmProperties.getTemperature());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("调用大模型失败，HTTP状态码: {}, 响应体: {}", response.getStatusCode(), response.getBody());
                return "大模型调用失败，HTTP状态码: " + response.getStatusCode();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choicesNode = root.path("choices");
            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                return "大模型返回格式异常，缺少 choices";
            }

            JsonNode contentNode = choicesNode.get(0).path("message").path("content");

            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return "大模型返回为空";
            }

            return contentNode.asText();

        } catch (HttpStatusCodeException e) {
            log.error(
                    "调用大模型失败，请求地址: {}, HTTP状态码: {}, 响应体: {}",
                    url,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            return "调用大模型异常: " + e.getStatusCode();
        } catch (Exception e) {
            log.error("调用大模型失败", e);
            return "调用大模型异常: " + e.getMessage();
        }
    }
}
