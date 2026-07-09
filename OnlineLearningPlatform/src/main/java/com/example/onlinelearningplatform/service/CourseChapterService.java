package com.example.onlinelearningplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinelearningplatform.dto.ChapterSaveDTO;
import com.example.onlinelearningplatform.dto.CourseDetailDTO;
import com.example.onlinelearningplatform.entity.CourseChapter;

public interface CourseChapterService extends IService<CourseChapter> {
    CourseDetailDTO getCourseDetail(Integer courseId);

    String addChapter(ChapterSaveDTO dto);

    String updateChapter(ChapterSaveDTO dto);

    String deleteChapter(Integer chapterId);

    String markStudied(Integer chapterId);
}
