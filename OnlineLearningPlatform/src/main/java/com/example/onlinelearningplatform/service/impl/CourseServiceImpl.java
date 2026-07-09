package com.example.onlinelearningplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.dto.*;
import com.example.onlinelearningplatform.entity.*;
import com.example.onlinelearningplatform.mapper.*;
import com.example.onlinelearningplatform.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 课程相关
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final SysUserMapper userMapper;
    private final StudentCourseRelMapper studentCourseRelMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final ChapterStudyRecordMapper chapterStudyRecordMapper;
    private final HomeworkMapper homeworkMapper;

    @Override
    public List<CourseDTO> courses() {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, true);
        List<Course> courses = baseMapper.selectList(wrapper);
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }

        List<CourseDTO> dtoList = new ArrayList<>();
        for (Course course : courses) {
            CourseDTO dto = new CourseDTO();
            dto.setId(course.getId());
            dto.setCourseName(course.getCourseName());
            dto.setCourseIntro(course.getCourseIntro());
            dto.setCreatedTime(course.getCreatedTime());

            SysUser user = userMapper.selectById(course.getTeacherId());
            if (user != null) {
                dto.setTeacherName(user.getUsername());
            }

            LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
            chapterWrapper.eq(CourseChapter::getCourseId, course.getId());
            Long count = courseChapterMapper.selectCount(chapterWrapper);
            dto.setTotalChapters(count.intValue());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public List<EnrolledCourseDTO> enrolledCourses() {
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<StudentCourseRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(StudentCourseRel::getStudentId, studentId);
        List<StudentCourseRel> rels = studentCourseRelMapper.selectList(relWrapper);
        if (rels.isEmpty()) {
            return new ArrayList<>();
        }

        List<EnrolledCourseDTO> result = new ArrayList<>();
        for (StudentCourseRel rel : rels) {
            Course course = baseMapper.selectById(rel.getCourseId());
            if (course == null) {
                continue;
            }

            EnrolledCourseDTO dto = new EnrolledCourseDTO();
            dto.setCourseId(course.getId());
            dto.setCourseName(course.getCourseName());
            dto.setCourseIntro(course.getCourseIntro());
            dto.setEnrolledTime(rel.getEnrolledTime());

            SysUser user = userMapper.selectById(course.getTeacherId());
            dto.setTeacherName(user != null ? user.getUsername() : "未知教师");

            LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
            chapterWrapper.eq(CourseChapter::getCourseId, course.getId());
            Long total = courseChapterMapper.selectCount(chapterWrapper);
            dto.setTotalChapters(total.intValue());

            LambdaQueryWrapper<CourseChapter> idWrapper = new LambdaQueryWrapper<>();
            idWrapper.eq(CourseChapter::getCourseId, course.getId()).select(CourseChapter::getId);
            List<Integer> chapterIds = courseChapterMapper.selectList(idWrapper).stream()
                    .map(CourseChapter::getId).collect(Collectors.toList());

            int studied = 0;
            if (!chapterIds.isEmpty()) {
                LambdaQueryWrapper<ChapterStudyRecord> studyWrapper = new LambdaQueryWrapper<>();
                studyWrapper.eq(ChapterStudyRecord::getStudentId, studentId)
                        .eq(ChapterStudyRecord::isStudied, true)
                        .in(ChapterStudyRecord::getChapterId, chapterIds);
                Long count = chapterStudyRecordMapper.selectCount(studyWrapper);
                studied = count != null ? count.intValue() : 0;
            }
            dto.setStudiedChapters(studied);

            int totalChapters = total.intValue();
            dto.setProgress(totalChapters == 0 ? 0 : studied * 100 / totalChapters);
            result.add(dto);
        }
        return result;
    }

    @Override
    public String enroll(Integer courseId) {
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId == null) {
            return "请先登录";
        }
        if (!"STUDENT".equals(UserContext.getCurrentRole())) {
            return "只有学生可以选课";
        }

        Course course = baseMapper.selectById(courseId);
        if (course == null || !Boolean.TRUE.equals(course.getStatus())) {
            return "课程不存在或已下架";
        }

        LambdaQueryWrapper<StudentCourseRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseRel::getStudentId, studentId)
                .eq(StudentCourseRel::getCourseId, courseId);
        if (studentCourseRelMapper.selectCount(wrapper) > 0) {
            return "已选过该课程";
        }

        StudentCourseRel rel = new StudentCourseRel();
        rel.setStudentId(studentId);
        rel.setCourseId(courseId);
        rel.setEnrolledTime(LocalDateTime.now());
        studentCourseRelMapper.insert(rel);
        return "选课成功";
    }

    @Override
    public List<CourseDTO> myCourses() {
        Integer teacherId = UserContext.getCurrentUserId();
        if (teacherId == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacherId).orderByDesc(Course::getCreatedTime);
        List<Course> courses = baseMapper.selectList(wrapper);

        List<CourseDTO> list = new ArrayList<>();
        for (Course course : courses) {
            CourseDTO dto = new CourseDTO();
            dto.setId(course.getId());
            dto.setCourseName(course.getCourseName());
            dto.setCourseIntro(course.getCourseIntro());
            dto.setCreatedTime(course.getCreatedTime());
            dto.setTeacherName(UserContext.getCurrentUsername());

            LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<>();
            chapterWrapper.eq(CourseChapter::getCourseId, course.getId());
            dto.setTotalChapters(courseChapterMapper.selectCount(chapterWrapper).intValue());
            list.add(dto);
        }
        return list;
    }

    @Override
    public String publishCourse(CoursePublishDTO dto) {
        Integer teacherId = UserContext.getCurrentUserId();
        if (teacherId == null || !"TEACHER".equals(UserContext.getCurrentRole())) {
            return "只有教师可以发布课程";
        }
        if (dto.getCourseName() == null || dto.getCourseName().isBlank()) {
            return "课程名称不能为空";
        }

        Course course = new Course();
        course.setTeacherId(teacherId);
        course.setCourseName(dto.getCourseName());
        course.setCourseIntro(dto.getCourseIntro());
        course.setStatus(true);
        course.setCreatedTime(LocalDateTime.now());
        baseMapper.insert(course);

        // 发布课程时顺便加一道默认作业
        Homework homework = new Homework();
        homework.setCourseId(course.getId());
        homework.setTeacherId(teacherId);
        homework.setQuestion("请结合本课程内容，谈谈你的学习体会与收获。");
        homework.setDeadline(LocalDateTime.now().plusDays(30));
        homework.setCreatedTime(LocalDateTime.now());
        homeworkMapper.insert(homework);
        return "发布成功";
    }

    @Override
    public String updateCourse(CourseUpdateDTO dto) {
        Integer teacherId = UserContext.getCurrentUserId();
        Course course = baseMapper.selectById(dto.getId());
        if (course == null) {
            return "课程不存在";
        }
        if (!course.getTeacherId().equals(teacherId)) {
            return "无权修改该课程";
        }

        course.setCourseName(dto.getCourseName());
        course.setCourseIntro(dto.getCourseIntro());
        return baseMapper.updateById(course) > 0 ? "更新成功" : "更新失败";
    }

    @Override
    public List<StudentListDTO> getStudents(Integer courseId) {
        Course course = baseMapper.selectById(courseId);
        if (course == null) {
            return new ArrayList<>();
        }
        if (!course.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<StudentCourseRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseRel::getCourseId, courseId);
        List<StudentCourseRel> rels = studentCourseRelMapper.selectList(wrapper);

        List<StudentListDTO> list = new ArrayList<>();
        for (StudentCourseRel rel : rels) {
            SysUser student = userMapper.selectById(rel.getStudentId());
            if (student == null) {
                continue;
            }
            StudentListDTO dto = new StudentListDTO();
            dto.setId(student.getId());
            dto.setUsername(student.getUsername());
            dto.setEnrolledTime(rel.getEnrolledTime());
            list.add(dto);
        }
        return list;
    }
}
