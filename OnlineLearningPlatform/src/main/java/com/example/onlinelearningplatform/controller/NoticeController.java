package com.example.onlinelearningplatform.controller;

import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.dto.NoticeDTO;
import com.example.onlinelearningplatform.dto.NoticePublishDTO;
import com.example.onlinelearningplatform.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 公告接口
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public Result list() {
        List<NoticeDTO> list = noticeService.listAll();
        return Result.success(list, "查询成功");
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Integer id) {
        NoticeDTO dto = noticeService.detail(id);
        if (dto == null) {
            return Result.error("公告不存在");
        }
        return Result.success(dto, "查询成功");
    }

    @PostMapping("/publish")
    public Result publish(@RequestBody NoticePublishDTO dto) {
        String msg = noticeService.publish(dto);
        if (!"发布成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }
}
