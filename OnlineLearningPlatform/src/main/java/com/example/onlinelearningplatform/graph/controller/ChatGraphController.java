package com.example.onlinelearningplatform.graph.controller;

import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.dto.ChatStreamDTO;
import com.example.onlinelearningplatform.service.AiChatService;
import com.example.onlinelearningplatform.util.AiErrorUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// AI问答
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatGraphController {

    private static final Logger log = LoggerFactory.getLogger(ChatGraphController.class);

    private final AiChatService aiChatService;

    @PostMapping
    public Result<?> chat(@RequestParam("sessionId") String sessionId,
                          @RequestParam("message") String message) {
        try {
            return Result.success(aiChatService.chat(sessionId, message), "对话成功");
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return Result.error(AiErrorUtil.toFriendlyMessage(e));
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPost(@RequestBody ChatStreamDTO dto) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiChatService.streamChat(dto.getSessionId(), dto.getMessage(), emitter);
        return emitter;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGet(@RequestParam("sessionId") String sessionId,
                                @RequestParam("message") String message) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiChatService.streamChat(sessionId, message, emitter);
        return emitter;
    }

    @DeleteMapping("/history")
    public Result clearHistory(@RequestParam("sessionId") String sessionId) {
        String msg = aiChatService.clearHistory(sessionId);
        return Result.success(msg);
    }
}
