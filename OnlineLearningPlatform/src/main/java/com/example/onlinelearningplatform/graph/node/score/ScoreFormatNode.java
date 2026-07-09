package com.example.onlinelearningplatform.graph.node.score;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 把AI返回的结果转成score和reason
public class ScoreFormatNode implements NodeAction {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String llmResponse = state.value("llmResponse", "");

        int score = 0;
        String reason = "无法解析评分结果";

        String jsonStr = extractJson(llmResponse);

        try {
            JsonNode rootNode = objectMapper.readTree(jsonStr);

            if (rootNode.has("score")) {
                score = rootNode.get("score").asInt();
            }
            if (rootNode.has("reason")) {
                reason = rootNode.get("reason").asText();
            }
        } catch (Exception e) {
            score = extractScoreByRegex(llmResponse);
            reason = extractReasonByRegex(llmResponse);
        }

        return Map.of("score", score, "reason", reason);
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private int extractScoreByRegex(String text) {
        Pattern pattern = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String extractReasonByRegex(String text) {
        Pattern pattern = Pattern.compile("\"reason\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        }
        return "无法提取评分理由";
    }
}
