package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDTO {
    private Integer id;
    private String title;
    private String content;
    private String publisherName;
    private String publisherRole;
    private Integer courseId;
    private LocalDateTime createdTime;
}
