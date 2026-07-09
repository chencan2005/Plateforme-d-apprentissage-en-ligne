package com.example.onlinelearningplatform.controller;

import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.dto.*;
import com.example.onlinelearningplatform.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 课程相关接口
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/list")
    public Result list() {
        List<CourseDTO> courses = courseService.courses();
        return Result.success(courses, "查询成功");
    }

    @PostMapping("/enrolled")
    public Result enrolled() {
        List<EnrolledCourseDTO> list = courseService.enrolledCourses();
        return Result.success(list, "查询成功");
    }

    @PostMapping("/enroll")
    public Result enroll(@RequestBody EnrollDTO dto) {
        String msg = courseService.enroll(dto.getCourseId());
        if (!"选课成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }

    @PostMapping("/my")
    public Result myCourses() {
        List<CourseDTO> list = courseService.myCourses();
        return Result.success(list, "查询成功");
    }

    @PostMapping("/publish")
    public Result publish(@RequestBody CoursePublishDTO dto) {
        String msg = courseService.publishCourse(dto);
        if (!"发布成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }

    @PutMapping("/update")
    public Result update(@RequestBody CourseUpdateDTO dto) {
        String msg = courseService.updateCourse(dto);
        if (!"更新成功".equals(msg)) {
            return Result.error(msg);
        }
        return Result.success(msg);
    }

    @PostMapping("/students")
    public Result students(@RequestBody IdDTO dto) {
        List<StudentListDTO> list = courseService.getStudents(dto.getId());
        return Result.success(list, "查询成功");
    }
}
