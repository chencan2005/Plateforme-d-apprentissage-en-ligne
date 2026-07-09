package com.example.onlinelearningplatform.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.util.AiErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

// AI打分（仅教师）
@RestController
@RequestMapping("/api/score")
public class ScoreGraphController {

    private static final Logger log = LoggerFactory.getLogger(ScoreGraphController.class);

    private final CompiledGraph scoreGraph;

    public ScoreGraphController(@Qualifier("scoreGraph") CompiledGraph scoreGraph) {
        this.scoreGraph = scoreGraph;
    }

    @PostMapping
    public Result<?> score(@RequestParam("content") String content) {
        if (!"TEACHER".equals(UserContext.getCurrentRole())) {
            return Result.error(403, "只有教师可以使用AI评分");
        }
        if (content == null || content.isBlank()) {
            return Result.error("评分内容不能为空");
        }
        try {
            Optional<OverAllState> result = scoreGraph.call(Map.of("content", content));
            if (result.isEmpty()) {
                return Result.error("AI评分失败");
            }
            Map<String, Object> data = result.get().data();
            if (data.containsKey("error")) {
                return Result.error(AiErrorUtil.toFriendlyMessage(
                        new RuntimeException(String.valueOf(data.get("error")))));
            }
            return Result.success(data, "评分成功");
        } catch (Exception e) {
            log.error("AI评分异常", e);
            return Result.error(AiErrorUtil.toFriendlyMessage(e));
        }
    }
}
