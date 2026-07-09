package com.example.onlinelearningplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.onlinelearningplatform.dto.NewPassword;
import com.example.onlinelearningplatform.dto.ProfileDTO;
import com.example.onlinelearningplatform.entity.SysUser;
import com.example.onlinelearningplatform.mapper.SysUserMapper;
import com.example.onlinelearningplatform.service.SysUserService;
import com.example.onlinelearningplatform.util.MD5Util;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    public ProfileDTO profile(Integer userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        ProfileDTO userProfile = new ProfileDTO();
        userProfile.setId(user.getId());
        userProfile.setUsername(user.getUsername());
        userProfile.setRole(user.getRole());
        userProfile.setCreatedTime(user.getCreatedTime());
        return userProfile;
    }

    @Override
    public String updatePassword(Integer userId, NewPassword newPassword) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            return "更新失败";
        }

        if (!user.getPassword().equals(MD5Util.encrypt(newPassword.getOldPassword()))) {
            return "旧密码错误";
        }
        user.setPassword(MD5Util.encrypt(newPassword.getNewPassword()));
        return baseMapper.updateById(user) > 0 ? "更新成功" : "更新失败";
    }
}
