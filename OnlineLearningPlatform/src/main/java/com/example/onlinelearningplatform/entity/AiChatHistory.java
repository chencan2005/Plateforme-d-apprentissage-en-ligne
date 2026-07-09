package com.example.onlinelearningplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_history")
public class AiChatHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String userQuestion;
    private String aiAnswer;
    private LocalDateTime chatTime;
}
