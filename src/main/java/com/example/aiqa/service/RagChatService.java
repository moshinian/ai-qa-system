package com.example.aiqa.service;

import com.example.aiqa.rag.model.KnowledgeChunk;
import com.example.aiqa.rag.retriever.SimpleKeywordRetriever;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagChatService {

    private final SimpleKeywordRetriever retriever;

    public RagChatService(SimpleKeywordRetriever retriever) {
        this.retriever = retriever;
    }

    public String ask(String question) {
        List<KnowledgeChunk> topChunks = retriever.retrieveTopK(question, 3);

        if (topChunks.isEmpty()) {
            return "知识库中未找到相关信息。";
        }

        String context = topChunks.stream()
                .map(chunk -> "【来源：" + chunk.getSource() + "】\n" + chunk.getContent())
                .collect(Collectors.joining("\n\n"));

        return buildMockAnswer(question, context);
    }

    private String buildMockAnswer(String question, String context) {
        return """
                这是基于知识库检索生成的模拟回答。

                问题：
                %s

                命中的知识上下文：
                %s
                """.formatted(question, context);
    }
}