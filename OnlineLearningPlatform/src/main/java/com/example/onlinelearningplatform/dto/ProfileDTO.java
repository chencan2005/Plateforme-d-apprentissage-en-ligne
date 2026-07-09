package com.example.onlinelearningplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProfileDTO {
    private Integer id;
    private String username;
    private String role;
    private LocalDateTime createdTime;
}