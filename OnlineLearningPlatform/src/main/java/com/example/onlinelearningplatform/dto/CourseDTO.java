package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Integer id;
    private String teacherName;
    private String courseName;
    private String courseIntro;
    private Integer totalChapters;
    private LocalDateTime createdTime;
}