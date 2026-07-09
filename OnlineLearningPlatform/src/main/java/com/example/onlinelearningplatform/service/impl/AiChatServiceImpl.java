package com.example.onlinelearningplatform.service.impl;

import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.entity.AiChatHistory;
import com.example.onlinelearningplatform.mapper.AiChatHistoryMapper;
import com.example.onlinelearningplatform.service.AiChatService;
import com.example.onlinelearningplatform.util.AiErrorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

// AI对话，带多轮记忆
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String SYSTEM_PROMPT = "你是一个在线学习平台的智能助手，请回答学生的问题。";

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;
    private final AiChatHistoryMapper chatHistoryMapper;
    private final ExecutorService aiChatExecutor;

    @Override
    public Map<String, Object> chat(String sessionId, String message) {
        String conversationId = buildConversationId(sessionId);
        String reply = callAi(message, conversationId);
        saveToDb(UserContext.getCurrentUserId(), message, reply);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("reply", reply);
        return result;
    }

    @Override
    public void streamChat(String sessionId, String message, SseEmitter emitter) {
        // 子线程里拿不到UserContext，先取userId
        Integer userId = UserContext.getCurrentUserId();
        String conversationId = userId + ":" + sessionId;

        aiChatExecutor.execute(() -> {
            try {
                ChatClient chatClient = buildChatClient();
                StringBuilder replyBuilder = new StringBuilder();

                chatClient.prompt()
                        .user(message)
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .stream()
                        .content()
                        .doOnNext(chunk -> {
                            replyBuilder.append(chunk);
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .blockLast();

                saveToDb(userId, message, replyBuilder.toString());
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(AiErrorUtil.toFriendlyMessage(e)));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });
    }

    @Override
    public String clearHistory(String sessionId) {
        chatMemory.clear(buildConversationId(sessionId));
        return "清空成功";
    }

    private String callAi(String message, String conversationId) {
        return buildChatClient().prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    private ChatClient buildChatClient() {
        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    private String buildConversationId(String sessionId) {
        return UserContext.getCurrentUserId() + ":" + sessionId;
    }

    private void saveToDb(Integer userId, String question, String answer) {
        AiChatHistory record = new AiChatHistory();
        record.setUserId(userId);
        record.setUserQuestion(question);
        record.setAiAnswer(answer);
        record.setChatTime(LocalDateTime.now());
        chatHistoryMapper.insert(record);
    }
}
