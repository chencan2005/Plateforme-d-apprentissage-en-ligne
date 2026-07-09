package com.example.onlinelearningplatform.graph.node.score;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

// 调AI打分
public class ScoreLLMNode implements NodeAction {

    private final ChatClient chatClient;

    public ScoreLLMNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String content = state.value("content", "");

        String prompt = """
                你是一个专业的评分助手。请对以下内容进行评分（0-100分），并给出评分理由。
                
                评分标准：
                1. 内容的完整性和准确性
                2. 逻辑性和条理性
                3. 语言表达的清晰度
                
                请严格按照以下JSON格式返回结果，不要包含其他内容：
                {"score": 分数, "reason": "评分理由"}
                
                待评分内容：
                """ + content;

        String response = chatClient.prompt().user(prompt).call().content();
        return Map.of("llmResponse", response);
    }
}
