package com.example.aiqa.dto;

import lombok.Data;

@Data
public class ChatResponse {

    private String answer;

    public ChatResponse() {
    }

    public ChatResponse(String answer) {
        this.answer = answer;
    }
}