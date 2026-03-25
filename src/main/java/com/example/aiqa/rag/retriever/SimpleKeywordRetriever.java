package com.example.aiqa.rag.retriever;

import com.example.aiqa.rag.loader.KnowledgeLoader;
import com.example.aiqa.rag.model.KnowledgeChunk;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SimpleKeywordRetriever {

    private final KnowledgeLoader knowledgeLoader;

    public SimpleKeywordRetriever(KnowledgeLoader knowledgeLoader) {
        this.knowledgeLoader = knowledgeLoader;
    }

    public List<KnowledgeChunk> retrieveTopK(String question, int k) {
        List<String> keywords = extractKeywords(question);

        Map<KnowledgeChunk, Integer> scoreMap = new HashMap<>();
        for (KnowledgeChunk chunk : knowledgeLoader.getKnowledgeChunks()) {
            int score = 0;
            String content = chunk.getContent().toLowerCase();

            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    score++;
                }
            }

            if (score > 0) {
                scoreMap.put(chunk, score);
            }
        }

        return scoreMap.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> extractKeywords(String question) {
        if (question == null || question.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = question.toLowerCase()
                .replace("？", " ")
                .replace("，", " ")
                .replace("。", " ")
                .replace(",", " ")
                .replace(".", " ");

        return Arrays.stream(normalized.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}