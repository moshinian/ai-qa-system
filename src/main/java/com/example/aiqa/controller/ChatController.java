package com.example.aiqa.controller;

import com.example.aiqa.dto.ChatRequest;
import com.example.aiqa.dto.ChatResponse;
import com.example.aiqa.dto.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @PostMapping("/ask")
    public Result<ChatResponse> ask(@RequestBody ChatRequest request) {
        String question = request.getQuestion();
        String answer = "你问的是：" + question + "；这是当前的模拟回复。";
        return Result.success(new ChatResponse(answer));
    }
}