package com.example.onlinelearningplatform.graph.node.score;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

// 检查参数
public class ScoreParamCheckNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String content = state.value("content", "");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        return Map.of("content", content);
    }
}
