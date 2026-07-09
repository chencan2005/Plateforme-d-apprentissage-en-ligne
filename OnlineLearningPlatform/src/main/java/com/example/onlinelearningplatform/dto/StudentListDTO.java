package com.example.onlinelearningplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentListDTO {
    private Integer id;
    private String username;
    private LocalDateTime enrolledTime;
}
