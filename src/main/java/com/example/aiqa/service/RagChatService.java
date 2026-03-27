package com.example.aiqa.service;

import com.example.aiqa.client.LlmClient;
import com.example.aiqa.rag.model.KnowledgeChunk;
import com.example.aiqa.rag.retriever.SimpleKeywordRetriever;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagChatService {

    private final SimpleKeywordRetriever retriever;
    private final LlmClient llmClient;

    public RagChatService(SimpleKeywordRetriever retriever, LlmClient llmClient) {
        this.retriever = retriever;
        this.llmClient = llmClient;
    }

    public String ask(String question) {
        List<KnowledgeChunk> topChunks = retriever.retrieveTopK(question, 3);

        if (topChunks.isEmpty()) {
            return "知识库中未找到相关信息。";
        }

        String context = topChunks.stream()
                .map(chunk -> "【来源：" + chunk.getSource() + "】\n" + chunk.getContent())
                .collect(Collectors.joining("\n\n"));

        String prompt = buildPrompt(question, context);
        return llmClient.chat(prompt);
    }

    private String buildPrompt(String question, String context) {
        return """
                你是一个基于本地知识库回答问题的 AI 助手。
                请严格根据提供的上下文回答问题，不要编造。
                如果上下文中没有足够信息，请明确说明“知识库中未提供足够信息”。

                知识上下文：
                %s

                用户问题：
                %s
                """.formatted(context, question);
    }
}