package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_summary")
public class KnowledgeSummary {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private String title;
    private String keyPoints;
    private String summaryContent;
    private LocalDateTime createdTime;
}
