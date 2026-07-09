package com.example.onlinelearningplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrolledCourseDTO {
    private Integer courseId;
    private String courseName;
    private String courseIntro;
    private String teacherName;
    private Integer totalChapters;
    private Integer studiedChapters;
    private Integer progress;
    private LocalDateTime enrolledTime;
}