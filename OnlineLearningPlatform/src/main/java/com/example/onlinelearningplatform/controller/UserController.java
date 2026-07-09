package com.example.onlinelearningplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.onlinelearningplatform.common.Result;
import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.dto.LoginDTO;
import com.example.onlinelearningplatform.dto.NewPassword;
import com.example.onlinelearningplatform.dto.ProfileDTO;
import com.example.onlinelearningplatform.entity.SysUser;
import com.example.onlinelearningplatform.service.SysUserService;
import com.example.onlinelearningplatform.util.JwtUtil;
import com.example.onlinelearningplatform.util.MD5Util;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 用户登录注册、个人信息
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;
    private final SysUserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody LoginDTO request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        if (request.getRole() == null ||
                (!"STUDENT".equals(request.getRole()) && !"TEACHER".equals(request.getRole()))) {
            return Result.error("角色必须是 STUDENT 或 TEACHER");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (userService.getOne(wrapper) != null) {
            return Result.error("用户已存在");
        }

        SysUser user = new SysUser();
        user.setPassword(MD5Util.encrypt(request.getPassword()));
        user.setRole(request.getRole());
        user.setUsername(request.getUsername());
        userService.save(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Result.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Result.error("密码不能为空");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = userService.getOne(wrapper);

        String encrypted = MD5Util.encrypt(request.getPassword());
        if (user == null || encrypted == null || !encrypted.equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Result.success(token, "登陆成功");
    }

    @PostMapping("/profile")
    public Result getUserInfo() {
        ProfileDTO profile = userService.profile(UserContext.getCurrentUserId());
        if (profile == null) {
            return Result.error("用户不存在");
        }
        return Result.success(profile, "查询成功");
    }

    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody NewPassword newPassword) {
        if (newPassword.getNewPassword() == null || newPassword.getNewPassword().length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        String msg = userService.updatePassword(UserContext.getCurrentUserId(), newPassword);
        if ("旧密码错误".equals(msg)) {
            return Result.error("旧密码错误");
        }
        if ("更新失败".equals(msg)) {
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }
}
