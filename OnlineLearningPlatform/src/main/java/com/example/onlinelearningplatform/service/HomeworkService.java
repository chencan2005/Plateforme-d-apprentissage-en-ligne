package com.example.onlinelearningplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinelearningplatform.dto.*;
import com.example.onlinelearningplatform.entity.Homework;

import java.util.List;
import java.util.Map;

public interface HomeworkService extends IService<Homework> {
    String publish(HomeworkPublishDTO dto);

    List<HomeworkTeacherDTO> teacherList(Integer courseId);

    List<HomeworkStudentDTO> studentList(Integer courseId);

    String submit(HomeworkSubmitDTO dto);

    List<HomeworkSubmissionDTO> submissions(Integer homeworkId);

    String grade(HomeworkGradeDTO dto);

    Map<String, Object> aiGrade(Integer submissionId);

    List<GradeDTO> grades(Integer courseId);
}
