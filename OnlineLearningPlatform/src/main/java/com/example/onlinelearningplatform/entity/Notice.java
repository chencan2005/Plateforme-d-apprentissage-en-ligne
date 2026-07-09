package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer publisherId;
    private Integer courseId;
    private LocalDateTime createdTime;
}
