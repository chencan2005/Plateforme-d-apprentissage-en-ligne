package com.example.onlinelearningplatform.dto;

import lombok.Data;

@Data
public class ChapterItemDTO {
    private Integer id;
    private String chapterTitle;
    private String contentType;
    private String content;
    private Integer sortOrder;
    private Boolean studied;
}
