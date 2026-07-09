package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chapter_study_record")
public class ChapterStudyRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer studentId;
    private Integer chapterId;

    @TableField("is_studied")
    private boolean studied;

    private LocalDateTime studyTime;
    private LocalDateTime createdTime;
}
