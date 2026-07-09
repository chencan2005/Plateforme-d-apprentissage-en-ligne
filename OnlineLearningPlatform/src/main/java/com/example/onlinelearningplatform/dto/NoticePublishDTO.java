package com.example.onlinelearningplatform.dto;

import lombok.Data;

@Data
public class NoticePublishDTO {
    private String title;
    private String content;
    private Integer courseId;
}
