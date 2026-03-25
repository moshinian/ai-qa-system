package com.example.aiqa.rag.splitter;

import com.example.aiqa.rag.model.KnowledgeChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextSplitter {

    public List<KnowledgeChunk> split(String source, String text, int maxLength) {
        List<KnowledgeChunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        text = text.replace("\r", "").trim();

        if (text.length() <= maxLength) {
            chunks.add(new KnowledgeChunk(source, text));
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());
            String chunkText = text.substring(start, end).trim();
            if (!chunkText.isBlank()) {
                chunks.add(new KnowledgeChunk(source, chunkText));
            }
            start = end;
        }

        return chunks;
    }
}