package com.example.onlinelearningplatform.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface AiChatService {
    Map<String, Object> chat(String sessionId, String message);

    void streamChat(String sessionId, String message, SseEmitter emitter);

    String clearHistory(String sessionId);
}
