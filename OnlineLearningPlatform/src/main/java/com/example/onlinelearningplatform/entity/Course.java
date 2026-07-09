package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer teacherId;
    private String courseName;
    private String courseIntro;
    private Boolean status;

    private LocalDateTime createdTime;
}