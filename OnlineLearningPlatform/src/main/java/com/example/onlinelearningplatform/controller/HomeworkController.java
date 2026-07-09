package com.example.onlinelearningplatform.controller;

import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.dto.*;
import com.example.onlinelearningplatform.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 作业相关接口
@RestController
@RequestMapping("/api/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    @PostMapping("/publish")
    public Result publish(@RequestBody HomeworkPublishDTO dto) {
        String msg = homeworkService.publish(dto);
        if (!"发布成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }

    @PostMapping("/teacherList")
    public Result teacherList(@RequestBody(required = false) IdDTO dto) {
        Integer courseId = dto != null ? dto.getId() : null;
        List<HomeworkTeacherDTO> list = homeworkService.teacherList(courseId);
        return Result.success(list, "查询成功");
    }

    @PostMapping("/studentList")
    public Result studentList(@RequestBody IdDTO dto) {
        List<HomeworkStudentDTO> list = homeworkService.studentList(dto.getId());
        return Result.success(list, "查询成功");
    }

    @PostMapping("/submit")
    public Result submit(@RequestBody HomeworkSubmitDTO dto) {
        String msg = homeworkService.submit(dto);
        if (!"提交成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }

    @PostMapping("/submissions")
    public Result submissions(@RequestBody IdDTO dto) {
        List<HomeworkSubmissionDTO> list = homeworkService.submissions(dto.getId());
        return Result.success(list, "查询成功");
    }

    @PutMapping("/grade")
    public Result grade(@RequestBody HomeworkGradeDTO dto) {
        String msg = homeworkService.grade(dto);
        if (!"批改成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }

    @PostMapping("/aiGrade")
    public Result aiGrade(@RequestBody IdDTO dto) {
        Map<String, Object> result = homeworkService.aiGrade(dto.getId());
        if (result.containsKey("error")) {
            return Result.error((String) result.get("error"));
        }
        return Result.success(result, "AI评分成功");
    }

    @PostMapping("/grades")
    public Result grades(@RequestBody(required = false) IdDTO dto) {
        Integer courseId = dto != null ? dto.getId() : null;
        List<GradeDTO> list = homeworkService.grades(courseId);
        return Result.success(list, "查询成功");
    }
}
