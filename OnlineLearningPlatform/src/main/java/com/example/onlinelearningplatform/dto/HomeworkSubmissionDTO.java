package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeworkSubmissionDTO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String answer;
    private LocalDateTime completedTime;
    private Integer score;
    private String feedback;
}
