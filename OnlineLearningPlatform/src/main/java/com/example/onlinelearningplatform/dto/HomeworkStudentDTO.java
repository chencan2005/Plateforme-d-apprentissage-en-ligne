package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeworkStudentDTO {
    private Integer id;
    private String question;
    private LocalDateTime deadline;
    private Boolean submitted;
    private String answer;
    private Integer score;
    private String feedback;
}
