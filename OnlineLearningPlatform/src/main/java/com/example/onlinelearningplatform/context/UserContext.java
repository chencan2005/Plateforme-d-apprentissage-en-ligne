package com.example.onlinelearningplatform.context;

// 存当前登录用户
public class UserContext {

    private static final ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        currentUserId.set(userId);
    }

    public static Integer getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    public static String getCurrentUsername() {
        return currentUsername.get();
    }

    public static void setCurrentRole(String role) {
        currentRole.set(role);
    }

    public static String getCurrentRole() {
        return currentRole.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUsername.remove();
        currentRole.remove();
    }
}
