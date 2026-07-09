package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("homework")
public class Homework {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer courseId;
    private Integer teacherId;
    private Integer studentId;
    private String question;
    private String answer;
    private LocalDateTime deadline;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
    private Integer score;
    private String feedback;
    private LocalDateTime gradedTime;
}
