package com.example.onlinelearningplatform.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.dto.*;
import com.example.onlinelearningplatform.entity.Course;
import com.example.onlinelearningplatform.entity.Homework;
import com.example.onlinelearningplatform.entity.StudentCourseRel;
import com.example.onlinelearningplatform.entity.SysUser;
import com.example.onlinelearningplatform.mapper.CourseMapper;
import com.example.onlinelearningplatform.mapper.HomeworkMapper;
import com.example.onlinelearningplatform.mapper.StudentCourseRelMapper;
import com.example.onlinelearningplatform.mapper.SysUserMapper;
import com.example.onlinelearningplatform.service.HomeworkService;
import com.example.onlinelearningplatform.util.AiErrorUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

// 作业业务，student_id为空是题目，非空是学生提交
@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements HomeworkService {

    private final CourseMapper courseMapper;
    private final SysUserMapper userMapper;
    private final StudentCourseRelMapper studentCourseRelMapper;
    private final CompiledGraph scoreGraph;

    public HomeworkServiceImpl(CourseMapper courseMapper,
                               SysUserMapper userMapper,
                               StudentCourseRelMapper studentCourseRelMapper,
                               @Qualifier("scoreGraph") CompiledGraph scoreGraph) {
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.studentCourseRelMapper = studentCourseRelMapper;
        this.scoreGraph = scoreGraph;
    }

    @Override
    public String publish(HomeworkPublishDTO dto) {
        if (!"TEACHER".equals(UserContext.getCurrentRole())) {
            return "只有教师可以发布作业";
        }
        if (dto.getQuestion() == null || dto.getQuestion().isBlank()) {
            return "作业题目不能为空";
        }
        if (dto.getDeadline() == null) {
            return "截止时间不能为空";
        }
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || !course.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return "无权在该课程发布作业";
        }

        Homework homework = new Homework();
        homework.setCourseId(dto.getCourseId());
        homework.setTeacherId(UserContext.getCurrentUserId());
        homework.setQuestion(dto.getQuestion());
        homework.setDeadline(dto.getDeadline());
        homework.setCreatedTime(LocalDateTime.now());
        return baseMapper.insert(homework) > 0 ? "发布成功" : "发布失败";
    }

    @Override
    public List<HomeworkTeacherDTO> teacherList(Integer courseId) {
        if (!"TEACHER".equals(UserContext.getCurrentRole())) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getTeacherId, UserContext.getCurrentUserId())
                .isNull(Homework::getStudentId)
                .orderByDesc(Homework::getCreatedTime);
        if (courseId != null) {
            wrapper.eq(Homework::getCourseId, courseId);
        }

        List<HomeworkTeacherDTO> list = new ArrayList<>();
        for (Homework template : baseMapper.selectList(wrapper)) {
            HomeworkTeacherDTO dto = new HomeworkTeacherDTO();
            dto.setId(template.getId());
            dto.setCourseId(template.getCourseId());
            dto.setQuestion(template.getQuestion());
            dto.setDeadline(template.getDeadline());

            Course course = courseMapper.selectById(template.getCourseId());
            dto.setCourseName(course != null ? course.getCourseName() : "");

            List<Homework> submissions = findSubmissions(template);
            dto.setSubmitCount(submissions.size());
            int graded = 0;
            for (Homework s : submissions) {
                if (s.getScore() != null) {
                    graded++;
                }
            }
            dto.setGradedCount(graded);
            list.add(dto);
        }
        return list;
    }

    @Override
    public List<HomeworkStudentDTO> studentList(Integer courseId) {
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId == null || !"STUDENT".equals(UserContext.getCurrentRole())) {
            return new ArrayList<>();
        }
        if (courseId == null || !isEnrolled(studentId, courseId)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getCourseId, courseId)
                .isNull(Homework::getStudentId)
                .orderByAsc(Homework::getCreatedTime);

        List<HomeworkStudentDTO> list = new ArrayList<>();
        for (Homework template : baseMapper.selectList(wrapper)) {
            HomeworkStudentDTO dto = new HomeworkStudentDTO();
            dto.setId(template.getId());
            dto.setQuestion(template.getQuestion());
            dto.setDeadline(template.getDeadline());

            Homework submission = findStudentSubmission(template, studentId);
            if (submission != null) {
                dto.setSubmitted(true);
                dto.setAnswer(submission.getAnswer());
                dto.setScore(submission.getScore());
                dto.setFeedback(submission.getFeedback());
            } else {
                dto.setSubmitted(false);
            }
            list.add(dto);
        }
        return list;
    }

    @Override
    public String submit(HomeworkSubmitDTO dto) {
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId == null || !"STUDENT".equals(UserContext.getCurrentRole())) {
            return "只有学生可以提交作业";
        }

        Homework template = baseMapper.selectById(dto.getHomeworkId());
        if (template == null || template.getStudentId() != null) {
            return "作业不存在";
        }
        if (template.getDeadline() == null) {
            return "作业截止时间未设置";
        }
        if (LocalDateTime.now().isAfter(template.getDeadline())) {
            return "已过截止时间";
        }
        if (!isEnrolled(studentId, template.getCourseId())) {
            return "请先选修该课程";
        }
        if (findStudentSubmission(template, studentId) != null) {
            return "已提交过该作业";
        }

        Homework submission = new Homework();
        submission.setCourseId(template.getCourseId());
        submission.setTeacherId(template.getTeacherId());
        submission.setStudentId(studentId);
        submission.setQuestion(template.getQuestion());
        submission.setAnswer(dto.getAnswer());
        submission.setDeadline(template.getDeadline());
        submission.setCreatedTime(template.getCreatedTime());
        submission.setCompletedTime(LocalDateTime.now());
        return baseMapper.insert(submission) > 0 ? "提交成功" : "提交失败";
    }

    @Override
    public List<HomeworkSubmissionDTO> submissions(Integer homeworkId) {
        Homework template = baseMapper.selectById(homeworkId);
        if (template == null || template.getStudentId() != null) {
            return new ArrayList<>();
        }
        if (!template.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return new ArrayList<>();
        }

        List<HomeworkSubmissionDTO> list = new ArrayList<>();
        for (Homework submission : findSubmissions(template)) {
            HomeworkSubmissionDTO dto = new HomeworkSubmissionDTO();
            dto.setId(submission.getId());
            dto.setStudentId(submission.getStudentId());
            dto.setAnswer(submission.getAnswer());
            dto.setCompletedTime(submission.getCompletedTime());
            dto.setScore(submission.getScore());
            dto.setFeedback(submission.getFeedback());

            SysUser student = userMapper.selectById(submission.getStudentId());
            dto.setStudentName(student != null ? student.getUsername() : "未知");
            list.add(dto);
        }
        return list;
    }

    @Override
    public String grade(HomeworkGradeDTO dto) {
        Homework submission = baseMapper.selectById(dto.getSubmissionId());
        if (submission == null || submission.getStudentId() == null) {
            return "提交记录不存在";
        }
        if (!submission.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return "无权批改";
        }

        submission.setScore(dto.getScore());
        submission.setFeedback(dto.getFeedback());
        submission.setGradedTime(LocalDateTime.now());
        return baseMapper.updateById(submission) > 0 ? "批改成功" : "批改失败";
    }

    @Override
    public Map<String, Object> aiGrade(Integer submissionId) {
        Homework submission = baseMapper.selectById(submissionId);
        if (submission == null || submission.getStudentId() == null) {
            return Map.of("error", "提交记录不存在");
        }
        if (!submission.getTeacherId().equals(UserContext.getCurrentUserId())) {
            return Map.of("error", "无权批改");
        }

        String content = "题目：" + submission.getQuestion() + "\n学生答案：" + submission.getAnswer();
        try {
            Optional<OverAllState> result = scoreGraph.call(Map.of("content", content));
            if (result.isEmpty()) {
                return Map.of("error", "AI评分失败");
            }
            Map<String, Object> data = result.get().data();
            if (data.containsKey("error")) {
                return Map.of("error", AiErrorUtil.toFriendlyMessage(
                        new RuntimeException(String.valueOf(data.get("error")))));
            }
            Integer score = parseScore(data.get("score"));
            String reason = data.get("reason") == null ? "" : data.get("reason").toString();

            submission.setScore(score);
            submission.setFeedback(reason);
            submission.setGradedTime(LocalDateTime.now());
            baseMapper.updateById(submission);

            Map<String, Object> resp = new HashMap<>();
            resp.put("score", score);
            resp.put("reason", reason);
            return resp;
        } catch (Exception e) {
            return Map.of("error", AiErrorUtil.toFriendlyMessage(e));
        }
    }

    @Override
    public List<GradeDTO> grades(Integer courseId) {
        Integer studentId = UserContext.getCurrentUserId();
        if (studentId == null || !"STUDENT".equals(UserContext.getCurrentRole())) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getStudentId, studentId)
                .isNotNull(Homework::getScore);
        if (courseId != null) {
            wrapper.eq(Homework::getCourseId, courseId);
        }

        List<GradeDTO> list = new ArrayList<>();
        for (Homework hw : baseMapper.selectList(wrapper)) {
            GradeDTO dto = new GradeDTO();
            dto.setCourseId(hw.getCourseId());
            dto.setQuestion(hw.getQuestion());
            dto.setScore(hw.getScore());
            dto.setFeedback(hw.getFeedback());
            dto.setGradedTime(hw.getGradedTime());

            Course course = courseMapper.selectById(hw.getCourseId());
            dto.setCourseName(course != null ? course.getCourseName() : "");
            list.add(dto);
        }
        return list;
    }

    private List<Homework> findSubmissions(Homework template) {
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getCourseId, template.getCourseId())
                .eq(Homework::getTeacherId, template.getTeacherId())
                .eq(Homework::getQuestion, template.getQuestion())
                .eq(Homework::getDeadline, template.getDeadline())
                .isNotNull(Homework::getStudentId);
        return baseMapper.selectList(wrapper);
    }

    private Homework findStudentSubmission(Homework template, Integer studentId) {
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getCourseId, template.getCourseId())
                .eq(Homework::getTeacherId, template.getTeacherId())
                .eq(Homework::getQuestion, template.getQuestion())
                .eq(Homework::getDeadline, template.getDeadline())
                .eq(Homework::getStudentId, studentId);
        return baseMapper.selectOne(wrapper);
    }

    private boolean isEnrolled(Integer studentId, Integer courseId) {
        LambdaQueryWrapper<StudentCourseRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseRel::getStudentId, studentId)
                .eq(StudentCourseRel::getCourseId, courseId);
        return studentCourseRelMapper.selectCount(wrapper) > 0;
    }

    private Integer parseScore(Object scoreObj) {
        if (scoreObj == null) {
            return 0;
        }
        try {
            double value = Double.parseDouble(scoreObj.toString().trim());
            int score = (int) Math.round(value);
            return Math.max(0, Math.min(100, score));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
