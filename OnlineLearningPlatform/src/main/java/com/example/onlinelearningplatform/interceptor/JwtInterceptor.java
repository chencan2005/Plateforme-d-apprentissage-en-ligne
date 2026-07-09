package com.example.onlinelearningplatform.interceptor;

import com.example.onlinelearningplatform.util.JwtUtil;
import com.example.onlinelearningplatform.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 校验token
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("没传token: {}", path);
            write401(response, "未登录或 Token 已过期");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("token无效或已过期: {}", path);
            write401(response, "登录已过期，请重新登录");
            return false;
        }

        try {
            UserContext.setCurrentUserId(jwtUtil.extractUserId(token));
            UserContext.setCurrentUsername(jwtUtil.extractUsername(token));
            UserContext.setCurrentRole(jwtUtil.extractRole(token));
            return true;
        } catch (Exception e) {
            log.warn("解析token失败: {}", path, e);
            write401(response, "登录已过期，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void write401(HttpServletResponse response, String message) {
        response.setStatus(401);
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
        } catch (Exception e) {
            log.error("写响应失败", e);
        }
    }
}
