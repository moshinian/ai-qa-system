package com.example.aiqa.rag.model;

import lombok.Data;

@Data
public class KnowledgeChunk {

    private String source;
    private String content;

    public KnowledgeChunk() {
    }

    public KnowledgeChunk(String source, String content) {
        this.source = source;
        this.content = content;
    }
}