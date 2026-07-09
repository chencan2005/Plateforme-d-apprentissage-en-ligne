package com.example.onlinelearningplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.dto.ChapterItemDTO;
import com.example.onlinelearningplatform.dto.ChapterSaveDTO;
import com.example.onlinelearningplatform.dto.CourseDetailDTO;
import com.example.onlinelearningplatform.entity.ChapterStudyRecord;
import com.example.onlinelearningplatform.entity.Course;
import com.example.onlinelearningplatform.entity.CourseChapter;
import com.example.onlinelearningplatform.entity.StudentCourseRel;
import com.example.onlinelearningplatform.entity.SysUser;
import com.example.onlinelearningplatform.mapper.ChapterStudyRecordMapper;
import com.example.onlinelearningplatform.mapper.CourseChapterMapper;
import com.example.onlinelearningplatform.mapper.CourseMapper;
import com.example.onlinelearningplatform.mapper.StudentCourseRelMapper;
import com.example.onlinelearningplatform.mapper.SysUserMapper;
import com.example.onlinelearningplatform.service.CourseChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 章节管理
@Service
@RequiredArgsConstructor
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements CourseChapterService {

    private final CourseMapper courseMapper;
    private final SysUserMapper userMapper;
    private final ChapterStudyRecordMapper studyRecordMapper;
    private final StudentCourseRelMapper studentCourseRelMapper;

    @Override
    public CourseDetailDTO getCourseDetail(Integer courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return null;
        }

        CourseDetailDTO detail = new CourseDetailDTO();
        detail.setCourseId(course.getId());
        detail.setCourseName(course.getCourseName());
        detail.setCourseIntro(course.getCourseIntro());

        SysUser teacher = userMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            detail.setTeacherName(teacher.getUsername());
        }

        LambdaQueryWrapper<CourseChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseChapter::getCourseId, courseId).orderByAsc(CourseChapter::getSortOrder);
        List<CourseChapter> chapters = baseMapper.selectList(wrapper);

        Set<Integer> studiedIds = new HashSet<>();
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId != null && "STUDENT".equals(UserContext.getCurrentRole())) {
            LambdaQueryWrapper<ChapterStudyRecord> studyWrapper = new LambdaQueryWrapper<>();
            studyWrapper.eq(ChapterStudyRecord::getStudentId, studentId)
                    .eq(ChapterStudyRecord::isStudied, true);
            for (ChapterStudyRecord record : studyRecordMapper.selectList(studyWrapper)) {
                studiedIds.add(record.getChapterId());
            }
        }

        List<ChapterItemDTO> items = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            ChapterItemDTO item = new ChapterItemDTO();
            item.setId(chapter.getId());
            item.setChapterTitle(chapter.getChapterTitle());
            item.setContentType(chapter.getContentType());
            item.setContent(chapter.getContent());
            item.setSortOrder(chapter.getSortOrder());
            item.setStudied(studiedIds.contains(chapter.getId()));
            items.add(item);
        }
        detail.setChapters(items);
        return detail;
    }

    @Override
    public String addChapter(ChapterSaveDTO dto) {
        if (!checkTeacherOwnCourse(dto.getCourseId())) {
            return "无权操作该课程";
        }

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(dto.getCourseId());
        chapter.setChapterTitle(dto.getChapterTitle());
        chapter.setContentType(dto.getContentType() == null ? "TEXT" : dto.getContentType());
        chapter.setContent(dto.getContent());
        chapter.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        chapter.setCreatedTime(LocalDateTime.now());
        return baseMapper.insert(chapter) > 0 ? "添加成功" : "添加失败";
    }

    @Override
    public String updateChapter(ChapterSaveDTO dto) {
        CourseChapter chapter = baseMapper.selectById(dto.getId());
        if (chapter == null) {
            return "章节不存在";
        }
        if (!checkTeacherOwnCourse(chapter.getCourseId())) {
            return "无权操作该课程";
        }

        chapter.setChapterTitle(dto.getChapterTitle());
        chapter.setContentType(dto.getContentType());
        chapter.setContent(dto.getContent());
        if (dto.getSortOrder() != null) {
            chapter.setSortOrder(dto.getSortOrder());
        }
        return baseMapper.updateById(chapter) > 0 ? "更新成功" : "更新失败";
    }

    @Override
    public String deleteChapter(Integer chapterId) {
        CourseChapter chapter = baseMapper.selectById(chapterId);
        if (chapter == null) {
            return "章节不存在";
        }
        if (!checkTeacherOwnCourse(chapter.getCourseId())) {
            return "无权操作该课程";
        }
        return baseMapper.deleteById(chapterId) > 0 ? "删除成功" : "删除失败";
    }

    @Override
    public String markStudied(Integer chapterId) {
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId == null || !"STUDENT".equals(UserContext.getCurrentRole())) {
            return "只有学生可以标记学习进度";
        }

        CourseChapter chapter = baseMapper.selectById(chapterId);
        if (chapter == null) {
            return "章节不存在";
        }
        if (!isEnrolled(studentId, chapter.getCourseId())) {
            return "请先选修该课程";
        }

        LambdaQueryWrapper<ChapterStudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChapterStudyRecord::getStudentId, studentId)
                .eq(ChapterStudyRecord::getChapterId, chapterId);
        ChapterStudyRecord record = studyRecordMapper.selectOne(wrapper);

        if (record == null) {
            record = new ChapterStudyRecord();
            record.setStudentId(studentId);
            record.setChapterId(chapterId);
            record.setStudied(true);
            record.setStudyTime(LocalDateTime.now());
            record.setCreatedTime(LocalDateTime.now());
            studyRecordMapper.insert(record);
        } else {
            record.setStudied(true);
            record.setStudyTime(LocalDateTime.now());
            studyRecordMapper.updateById(record);
        }
        return "标记成功";
    }

    private boolean checkTeacherOwnCourse(Integer courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return false;
        }
        return course.getTeacherId().equals(UserContext.getCurrentUserId());
    }

    private boolean isEnrolled(Integer studentId, Integer courseId) {
        LambdaQueryWrapper<StudentCourseRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseRel::getStudentId, studentId)
                .eq(StudentCourseRel::getCourseId, courseId);
        return studentCourseRelMapper.selectCount(wrapper) > 0;
    }
}
