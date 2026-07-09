package com.example.onlinelearningplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinelearningplatform.dto.NoticeDTO;
import com.example.onlinelearningplatform.dto.NoticePublishDTO;
import com.example.onlinelearningplatform.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    List<NoticeDTO> listAll();

    NoticeDTO detail(Integer id);

    String publish(NoticePublishDTO dto);
}
