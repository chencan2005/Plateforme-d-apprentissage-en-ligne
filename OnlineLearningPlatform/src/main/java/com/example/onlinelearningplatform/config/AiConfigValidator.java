package com.example.onlinelearningplatform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AiConfigValidator implements ApplicationRunner {

    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;

    @Override
    public void run(ApplicationArguments args) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            log.warn("没配DashScope API Key，AI功能用不了，去application-local.yml里填一下");
        }
    }
}
