package com.example.onlinelearningplatform.dto;

import lombok.Data;

@Data
public class SummarySaveDTO {
    private String title;
    private String keyPoints;
    private String summaryContent;
    private Integer courseId;
}
