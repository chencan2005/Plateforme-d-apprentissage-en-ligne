package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_chapter")
public class CourseChapter {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer courseId;
    private String chapterTitle;
    private String contentType;
    private String content;
    private Integer sortOrder;

    private LocalDateTime createdTime;
}
