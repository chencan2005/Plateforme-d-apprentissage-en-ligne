package com.example.onlinelearningplatform.controller;

import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.dto.SummaryResultDTO;
import com.example.onlinelearningplatform.dto.SummarySaveDTO;
import com.example.onlinelearningplatform.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// 知识点总结
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            SummaryResultDTO result = summaryService.summarize(file);
            return Result.success(result, "总结成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/save")
    public Result save(@RequestBody SummarySaveDTO dto) {
        String msg = summaryService.save(dto);
        if (!"保存成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }
}
