package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeworkTeacherDTO {
    private Integer id;
    private Integer courseId;
    private String courseName;
    private String question;
    private LocalDateTime deadline;
    private Integer submitCount;
    private Integer gradedCount;
}
