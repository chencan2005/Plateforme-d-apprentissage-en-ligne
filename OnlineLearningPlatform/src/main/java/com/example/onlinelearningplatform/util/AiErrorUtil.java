package com.example.onlinelearningplatform.util;

// AI报错转中文提示
public final class AiErrorUtil {

    private AiErrorUtil() {
    }

    public static String toFriendlyMessage(Throwable e) {
        if (e == null) {
            return "AI服务暂时不可用";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null) {
            msg = msg + " " + cause.getMessage();
        }

        if (msg.contains("401") || msg.contains("Unauthorized") || msg.contains("InvalidApiKey")) {
            return "AI Key没配或不对，检查application-local.yml";
        }
        if (msg.contains("DASHSCOPE_API_KEY") || msg.contains("api-key") || msg.contains("ApiKey")) {
            return "AI Key没配置";
        }
        if (msg.contains("model") || msg.contains("Model")) {
            return "AI模型名配错了，试试qwen-plus";
        }
        return "AI服务暂时不可用";
    }
}
