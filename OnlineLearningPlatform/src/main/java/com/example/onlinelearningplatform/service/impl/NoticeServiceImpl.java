package com.example.onlinelearningplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.dto.NoticeDTO;
import com.example.onlinelearningplatform.dto.NoticePublishDTO;
import com.example.onlinelearningplatform.entity.Course;
import com.example.onlinelearningplatform.entity.Notice;
import com.example.onlinelearningplatform.entity.SysUser;
import com.example.onlinelearningplatform.mapper.CourseMapper;
import com.example.onlinelearningplatform.mapper.NoticeMapper;
import com.example.onlinelearningplatform.mapper.SysUserMapper;
import com.example.onlinelearningplatform.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 公告
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final SysUserMapper userMapper;
    private final CourseMapper courseMapper;

    @Override
    public List<NoticeDTO> listAll() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notice::getCreatedTime);
        List<NoticeDTO> list = new ArrayList<>();
        for (Notice notice : baseMapper.selectList(wrapper)) {
            list.add(toDTO(notice));
        }
        return list;
    }

    @Override
    public NoticeDTO detail(Integer id) {
        Notice notice = baseMapper.selectById(id);
        if (notice == null) {
            return null;
        }
        return toDTO(notice);
    }

    @Override
    public String publish(NoticePublishDTO dto) {
        if (!"TEACHER".equals(UserContext.getCurrentRole())) {
            return "只有教师可以发布公告";
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            return "标题不能为空";
        }
        if (dto.getCourseId() != null) {
            Course course = courseMapper.selectById(dto.getCourseId());
            if (course == null) {
                return "关联课程不存在";
            }
            if (!course.getTeacherId().equals(UserContext.getCurrentUserId())) {
                return "无权在该课程发布公告";
            }
        }

        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setPublisherId(UserContext.getCurrentUserId());
        notice.setCourseId(dto.getCourseId());
        notice.setCreatedTime(LocalDateTime.now());
        return baseMapper.insert(notice) > 0 ? "发布成功" : "发布失败";
    }

    private NoticeDTO toDTO(Notice notice) {
        NoticeDTO dto = new NoticeDTO();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setCourseId(notice.getCourseId());
        dto.setCreatedTime(notice.getCreatedTime());

        SysUser publisher = userMapper.selectById(notice.getPublisherId());
        if (publisher != null) {
            dto.setPublisherName(publisher.getUsername());
            dto.setPublisherRole(publisher.getRole());
        }
        return dto;
    }
}
