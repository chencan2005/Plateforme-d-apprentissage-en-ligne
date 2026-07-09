package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseDetailDTO {
    private Integer courseId;
    private String courseName;
    private String courseIntro;
    private String teacherName;
    private List<ChapterItemDTO> chapters;
}
