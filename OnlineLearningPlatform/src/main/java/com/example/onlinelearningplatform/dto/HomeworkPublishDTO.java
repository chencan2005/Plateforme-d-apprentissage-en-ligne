package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeworkPublishDTO {
    private Integer courseId;
    private String question;
    private LocalDateTime deadline;
}
