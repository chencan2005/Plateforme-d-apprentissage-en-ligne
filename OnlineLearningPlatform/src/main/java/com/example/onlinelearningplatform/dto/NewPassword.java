package com.example.onlinelearningplatform.dto;

import lombok.Data;

@Data
public class NewPassword {
    private String oldPassword;
    private String newPassword;
}