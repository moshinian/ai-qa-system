package com.example.aiqa.rag.loader;

import com.example.aiqa.rag.model.KnowledgeChunk;
import com.example.aiqa.rag.splitter.TextSplitter;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeLoader {

    private final TextSplitter textSplitter;
    private final List<KnowledgeChunk> knowledgeChunks = new ArrayList<>();

    public KnowledgeLoader(TextSplitter textSplitter) {
        this.textSplitter = textSplitter;
    }

    @PostConstruct
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:knowledge/*.txt");

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                try (InputStream inputStream = resource.getInputStream()) {
                    String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    List<KnowledgeChunk> chunks = textSplitter.split(filename, text, 120);
                    knowledgeChunks.addAll(chunks);
                }
            }

            System.out.println("知识库加载完成，chunk 数量：" + knowledgeChunks.size());
        } catch (Exception e) {
            throw new RuntimeException("加载知识库失败: " + e.getMessage(), e);
        }
    }

    public List<KnowledgeChunk> getKnowledgeChunks() {
        return knowledgeChunks;
    }
}