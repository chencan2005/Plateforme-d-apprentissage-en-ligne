package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradeDTO {
    private Integer courseId;
    private String courseName;
    private String question;
    private Integer score;
    private String feedback;
    private LocalDateTime gradedTime;
}
