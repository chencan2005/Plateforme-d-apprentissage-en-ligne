package com.example.onlinelearningplatform.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// MD5加密
public class MD5Util {

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不存在", e);
        }
    }

    public static String encryptWithSalt(String plainText, String salt) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        return encrypt(plainText + (salt == null ? "" : salt));
    }

    public static boolean verify(String plainText, String encrypted) {
        if (plainText == null || encrypted == null) {
            return false;
        }
        return encrypt(plainText).equals(encrypted);
    }

    public static boolean verifyWithSalt(String plainText, String salt, String encrypted) {
        if (plainText == null || encrypted == null) {
            return false;
        }
        return encryptWithSalt(plainText, salt).equals(encrypted);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
