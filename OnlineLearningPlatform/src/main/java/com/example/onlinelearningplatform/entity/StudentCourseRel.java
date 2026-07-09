package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_course_rel")
public class StudentCourseRel {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer studentId;
    private Integer courseId;

    private LocalDateTime enrolledTime;
}
