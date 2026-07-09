package com.example.onlinelearningplatform.dto;

import lombok.Data;

@Data
public class HomeworkGradeDTO {
    private Integer submissionId;
    private Integer score;
    private String feedback;
}
