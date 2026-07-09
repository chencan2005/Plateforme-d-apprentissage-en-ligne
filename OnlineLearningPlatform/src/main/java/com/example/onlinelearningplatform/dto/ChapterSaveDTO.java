package com.example.onlinelearningplatform.dto;

import lombok.Data;

@Data
public class ChapterSaveDTO {
    private Integer id;
    private Integer courseId;
    private String chapterTitle;
    private String contentType;
    private String content;
    private Integer sortOrder;
}
