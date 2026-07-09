package com.example.onlinelearningplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.onlinelearningplatform.mapper")
public class OnlineLearningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineLearningPlatformApplication.class, args);
    }

}
