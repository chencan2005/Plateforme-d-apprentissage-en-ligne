package com.example.onlinelearningplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.onlinelearningplatform.dto.NewPassword;
import com.example.onlinelearningplatform.dto.ProfileDTO;
import com.example.onlinelearningplatform.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    ProfileDTO profile(Integer userId);
    String updatePassword(Integer userId, NewPassword newPassword);
}
