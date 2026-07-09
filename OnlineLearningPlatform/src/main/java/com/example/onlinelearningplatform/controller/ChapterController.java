package com.example.onlinelearningplatform.controller;

import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.dto.ChapterSaveDTO;
import com.example.onlinelearningplatform.dto.CourseDetailDTO;
import com.example.onlinelearningplatform.dto.IdDTO;
import com.example.onlinelearningplatform.service.CourseChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 章节相关接口
@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final CourseChapterService chapterService;

    @PostMapping("/detail")
    public Result detail(@RequestBody IdDTO dto) {
        CourseDetailDTO detail = chapterService.getCourseDetail(dto.getId());
        if (detail == null) {
            return Result.error("课程不存在");
        }
        return Result.success(detail, "查询成功");
    }

    @PostMapping("/add")
    public Result add(@RequestBody ChapterSaveDTO dto) {
        return toResult(chapterService.addChapter(dto));
    }

    @PutMapping("/update")
    public Result update(@RequestBody ChapterSaveDTO dto) {
        return toResult(chapterService.updateChapter(dto));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return toResult(chapterService.deleteChapter(id));
    }

    @PostMapping("/markStudied")
    public Result markStudied(@RequestBody IdDTO dto) {
        return toResult(chapterService.markStudied(dto.getId()));
    }

    private Result toResult(String msg) {
        if (msg != null && msg.endsWith("成功")) {
            return Result.success(msg);
        }
        return Result.error(msg == null ? "操作失败" : msg);
    }
}
