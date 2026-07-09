package com.example.onlinelearningplatform.graph.config;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.onlinelearningplatform.graph.node.score.ScoreFormatNode;
import com.example.onlinelearningplatform.graph.node.score.ScoreLLMNode;
import com.example.onlinelearningplatform.graph.node.score.ScoreParamCheckNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

// AI评分流程：校验 -> 打分 -> 格式化
@Configuration
public class GraphConfig {

    private final ChatClient.Builder chatClientBuilder;

    public GraphConfig(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    @Bean("scoreGraph")
    public CompiledGraph scoreGraph() throws GraphStateException {
        KeyStrategyFactory strategyFactory = () -> Map.of(
                "content", new ReplaceStrategy(),
                "llmResponse", new ReplaceStrategy(),
                "score", new ReplaceStrategy(),
                "reason", new ReplaceStrategy()
        );

        StateGraph stateGraph = new StateGraph("scoreGraph", strategyFactory);

        stateGraph.addNode("参数校验", AsyncNodeAction.node_async(new ScoreParamCheckNode()));
        stateGraph.addNode("AI打分", AsyncNodeAction.node_async(new ScoreLLMNode(chatClientBuilder)));
        stateGraph.addNode("格式化结果", AsyncNodeAction.node_async(new ScoreFormatNode()));

        stateGraph.addEdge(StateGraph.START, "参数校验");
        stateGraph.addEdge("参数校验", "AI打分");
        stateGraph.addEdge("AI打分", "格式化结果");
        stateGraph.addEdge("格式化结果", StateGraph.END);

        return stateGraph.compile();
    }
}
