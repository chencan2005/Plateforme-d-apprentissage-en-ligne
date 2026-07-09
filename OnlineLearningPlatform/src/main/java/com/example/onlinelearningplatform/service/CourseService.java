package com.example.onlinelearningplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinelearningplatform.dto.*;
import com.example.onlinelearningplatform.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {
    List<CourseDTO> courses();

    List<EnrolledCourseDTO> enrolledCourses();

    String enroll(Integer courseId);

    List<CourseDTO> myCourses();

    String publishCourse(CoursePublishDTO dto);

    String updateCourse(CourseUpdateDTO dto);

    List<StudentListDTO> getStudents(Integer courseId);
}
